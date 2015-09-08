package co.touchlab.squeaky.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import co.touchlab.squeaky.Config;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import co.touchlab.squeaky.table.TableInfo;
import co.touchlab.squeaky.table.TransientCache;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal Dao implementation.  Loads and uses the generated code.  You probably don't want to create this directly.
 *
 * @author graywatson, kgalligan
 */
public class ModelDao<T, ID> implements Dao<T, ID>
{
	private final Class<T> entityClass;
	private final GeneratedTableMapper<T, ID> generatedTableMapper;
	private final Set<DaoObserver> daoObserverSet = Collections.newSetFromMap(new ConcurrentHashMap<DaoObserver, Boolean>());
	private final String[] tableCols;
	private final SqueakyContext openHelperHelper;
	private final FieldType idFieldType;

	private SQLiteStatement createStatement;
	private SQLiteStatement updateStatement;

	protected ModelDao(SqueakyContext openHelper, Class<T> entityClass, GeneratedTableMapper<T, ID> generatedTableMapper)
	{
		this.openHelperHelper = openHelper;
		this.entityClass = entityClass;
		try
		{
			this.generatedTableMapper = generatedTableMapper;

			FieldType idField = null;
			FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
			for (FieldType fieldType : fieldTypes)
			{
				if(fieldType.isId() || fieldType.isGeneratedId())
				{
					idField = fieldType;
					break;
				}
			}

			idFieldType = idField;

			tableCols = buildSelect();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void cleanUp()
	{
		if(createStatement != null)
			createStatement.close();
		if(updateStatement != null)
			updateStatement.close();
	}

	private String[] buildSelect() throws SQLException
	{
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		String[] selectList = new String[fieldTypes.length];

		for (int i=0; i<fieldTypes.length; i++)
		{
			selectList[i] = fieldTypes[i].getColumnName();
		}
		return selectList;
	}

	public T queryForId(ID id) throws SQLException
	{
		List<T> tList = queryForEq(idFieldType.getColumnName(), id);
		return tList.size() == 0 ? null : tList.get(0);
	}

	public List<T> queryForAll() throws SQLException
	{
		return makeCursorResults(null, null, null);
	}

	public List<T> queryForEq(String fieldName, Object value) throws SQLException
	{
		return makeCursorResults(fieldName + " = ?", new String[]{value.toString()}, null);
	}

	public List<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException
	{
		StringBuilder query = new StringBuilder();
		String[] args = new String[fieldValues.size()];

		int count =0;
		for (String field : fieldValues.keySet())
		{
			if(query.length() > 0)
				query.append(" and ");
			query.append(field).append(" = ?");
			Object val = fieldValues.get(field);
			args[count++] = val == null ? null : val.toString();
		}

		return makeCursorResults(query.toString(), args, null);
	}

	public Where<T, ID> createWhere() throws SQLException
	{
		return new Where<T, ID>(this);
	}

	private List<T> makeCursorResults(String where, String[] args, String orderBy) throws SQLException
	{
		List<T> results = new ArrayList<T>();
		TransientCache objectCache = new TransientCache();
		Cursor cursor = openHelperHelper.getHelper().getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, where, args, null, null, orderBy);
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					T object = generatedTableMapper.createObject(cursor);
					generatedTableMapper.fillRow(object, cursor, this, Config.MAX_AUTO_REFRESH, objectCache);
					results.add(object);
				} while (cursor.moveToNext());
			}
		}
		finally
		{
			cursor.close();
		}

		return results;
	}

	public List<T> query(String where, String orderBy) throws SQLException
	{
		return makeCursorResults(where, null, orderBy);
	}

	public List<T> query(String where)throws SQLException
	{
		return query(where, null);
	}

	public List<T> query(Where<T, ID> where, String orderBy)throws SQLException
	{
		String statement = where.getStatement();
		return query(statement, orderBy);
	}

	public List<T> query(Where<T, ID> where)throws SQLException
	{
		return query(where, null);
	}

	public void create(T data) throws SQLException
	{
		SQLiteStatement sqLiteStatement = makeCreateStatement();

		generatedTableMapper.bindCreateVals(sqLiteStatement, data);

		long newRowId = sqLiteStatement.executeInsert();

		if(idFieldType != null && idFieldType.isGeneratedId())
		{
			generatedTableMapper.assignId(data, newRowId);
		}

		notifyChanges();
	}

	private synchronized SQLiteStatement makeCreateStatement() throws SQLException
	{
		if(createStatement == null)
		{
			SQLiteDatabase db = openHelperHelper.getHelper().getWritableDatabase();
			TableInfo<T> tableConfig = generatedTableMapper.getTableConfig();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into ");
			sb.append(tableConfig.getTableName());
			sb.append("(");
			boolean first = true;
			StringBuilder args= new StringBuilder();
			for (FieldType fieldType : generatedTableMapper.getTableConfig().getFieldTypes())
			{
				if(!fieldType.isGeneratedId())
				{
					if(!first)
					{
						sb.append(",");
						args.append(",");
					}
					sb.append(fieldType.getColumnName());
					args.append("?");
					first = false;
				}
			}
			sb.append(")values(").append(args.toString()).append(")");

			createStatement = db.compileStatement(sb.toString());
		}

		return createStatement;
	}

	private void fillContentVal(ContentValues values, FieldType fieldType, Object val)
	{
		if(val instanceof String)
			values.put(fieldType.getColumnName(), (String)val);
		else if(val instanceof Integer)
			values.put(fieldType.getColumnName(), (Integer)val);
		else if(val instanceof Long)
			values.put(fieldType.getColumnName(), (Long)val);
		else if(val instanceof Byte)
			values.put(fieldType.getColumnName(), (Byte)val);
		else if(val instanceof Short)
			values.put(fieldType.getColumnName(), (Short)val);
		else if(val instanceof Float)
			values.put(fieldType.getColumnName(), (Float)val);
		else if(val instanceof Double)
			values.put(fieldType.getColumnName(), (Double)val);
		else if(val instanceof Boolean)
			values.put(fieldType.getColumnName(), (Boolean)val);
		else if(val instanceof byte[])
			values.put(fieldType.getColumnName(), (byte[])val);
		else
			throw new IllegalArgumentException("Don't recognize type for: "+ val);
	}

	public T createIfNotExists(T data) throws SQLException
	{
		if (data == null) {
			return null;
		}
		T existing = queryForId(generatedTableMapper.extractId(data));
		if (existing == null) {
			create(data);
			return data;
		} else {
			return existing;
		}
	}

	public void createOrUpdate(T data) throws SQLException
	{
		ID id = extractId(data);
		// assume we need to create it if there is no id
		if (id == null || !idExists(id)) {
			create(data);
		} else {
			update(data);
		}
	}

	public void update(T data) throws SQLException
	{
		SQLiteStatement sqLiteStatement = makeUpdateStatement();

		generatedTableMapper.bindVals(sqLiteStatement, data);

		sqLiteStatement.executeUpdateDelete();

		notifyChanges();
	}

	private synchronized SQLiteStatement makeUpdateStatement() throws SQLException
	{
		if(updateStatement == null)
		{
			SQLiteDatabase db = openHelperHelper.getHelper().getWritableDatabase();
			TableInfo<T> tableConfig = generatedTableMapper.getTableConfig();
			StringBuilder sb = new StringBuilder();
			sb.append("update ").append(tableConfig.getTableName()).append(" set ");
			boolean first = true;

			for (FieldType fieldType : generatedTableMapper.getTableConfig().getFieldTypes())
			{
				if(!fieldType.isGeneratedId() && !fieldType.isId())
				{
					if(!first)
					{
						sb.append(",");
					}
					sb.append(fieldType.getColumnName()).append(" = ?");
					first = false;
				}
			}

			sb.append(" where ").append(generatedTableMapper.getTableConfig().idField.getColumnName()).append(" = ?");

			updateStatement = db.compileStatement(sb.toString());
		}

		return updateStatement;
	}

	public int updateId(T data, ID newId) throws SQLException
	{
		SQLiteDatabase db = openHelperHelper.getHelper().getWritableDatabase();

		ContentValues values = new ContentValues();

		fillContentVal(values, idFieldType, newId);

		int result = db.update(generatedTableMapper.getTableConfig().getTableName(), values, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()});

		notifyChanges();

		return result;
	}

	public void refresh(T data) throws SQLException
	{
		refresh(data, Config.MAX_AUTO_REFRESH);
	}

	public void refresh(T data, Integer recursiveAutorefreshCountdown) throws SQLException
	{
		Cursor cursor = openHelperHelper.getHelper().getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()}, null, null, null);
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					generatedTableMapper.fillRow(data, cursor, this, recursiveAutorefreshCountdown, null);
				} while (cursor.moveToNext());
			}
		}
		finally
		{
			cursor.close();
		}
	}

	public int delete(T data) throws SQLException
	{
		return data == null ? 0 : deleteById(generatedTableMapper.extractId(data));
	}

	public int deleteById(ID id) throws SQLException
	{
		int result = openHelperHelper.getHelper().getWritableDatabase().delete(generatedTableMapper.getTableConfig().getTableName(), idFieldType.getColumnName() + "= ?", new String[]{id.toString()});

		notifyChanges();

		return result;
	}

	public int delete(Collection<T> datas) throws SQLException
	{
		List<ID> ids = new ArrayList<ID>(datas.size());
		for (T data : datas)
		{
			ids.add(generatedTableMapper.extractId(data));
		}

		return deleteIds(ids);
	}

	//TODO: Delete with in statement
	public int deleteIds(Collection<ID> ids) throws SQLException
	{
		return delete(createWhere().in(idFieldType.getColumnName(), ids));
	}

	public int delete(Where<T, ID> where) throws SQLException
	{
		int result = openHelperHelper.getHelper().getWritableDatabase().delete(generatedTableMapper.getTableConfig().getTableName(), where.getStatement(), null);

		notifyChanges();

		return result;
	}

	public CloseableIterator<T> iterator() throws SQLException
	{
		return new SelectIterator<T, ID>(
				openHelperHelper.getHelper().getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, null, null, null, null, null),
				ModelDao.this
		);
	}

	public CloseableIterator<T> iterator(Where<T, ID> where) throws SQLException
	{
		return new SelectIterator<T, ID>(
				openHelperHelper.getHelper().getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, where.getStatement(), null, null, null, null),
				ModelDao.this
		);
	}

	public long queryRawValue(String query, String... arguments) throws SQLException
	{
		return DatabaseUtils.longForQuery(openHelperHelper.getHelper().getWritableDatabase(), query, arguments);
	}

	/*private void assignStatementArguments(CompiledStatement compiledStatement, String[] arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++) {
			compiledStatement.setObject(i, arguments[i], SqlType.STRING);
		}
	}*/


	public String objectToString(T data)throws SQLException
	{
		return generatedTableMapper.objectToString(data);
	}

	public boolean objectsEqual(T data1, T data2) throws SQLException
	{
		return generatedTableMapper.objectsEqual(data1, data2);
	}

	public ID extractId(T data) throws SQLException
	{
		return generatedTableMapper.extractId(data);
	}

	public void fillForeignCollection(T data, String fieldName)throws SQLException
	{
		generatedTableMapper.fillForeignCollection(data, this, fieldName);
	}

	public Class<T> getDataClass()
	{
		return entityClass;
	}

	public boolean isUpdatable()
	{
		return true;
	}

	public long countOf() throws SQLException
	{
		return DatabaseUtils.queryNumEntries(openHelperHelper.getHelper().getWritableDatabase(), generatedTableMapper.getTableConfig().getTableName());
	}

	public long countOf(Where<T, ID> where) throws SQLException
	{
		return DatabaseUtils.longForQuery(openHelperHelper.getHelper().getWritableDatabase(), "select count(*) from "+ generatedTableMapper.getTableConfig().getTableName() +" where "+ where.getStatement(), null);
	}

	//TODO could be faster
	public boolean idExists(ID id) throws SQLException
	{
		return queryForId(id) != null;
	}

	public void registerObserver(DaoObserver observer)
	{
		daoObserverSet.add(observer);
	}

	public void unregisterObserver(DaoObserver observer)
	{
		daoObserverSet.remove(observer);
	}

	public void notifyChanges()
	{
		Iterator<DaoObserver> iterator = daoObserverSet.iterator();
		while (iterator.hasNext())
		{
			DaoObserver next =  daoObserverSet.iterator().next();
			next.onChange();
		}
	}

	public GeneratedTableMapper<T, ID> getGeneratedTableMapper()
	{
		return generatedTableMapper;
	}

	public SqueakyContext getOpenHelper()
	{
		return openHelperHelper;
	}
}
