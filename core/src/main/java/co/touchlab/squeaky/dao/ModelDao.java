package co.touchlab.squeaky.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import co.touchlab.squeaky.Config;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.ForeignCollectionInfo;
import co.touchlab.squeaky.sql.SqlHelper;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import co.touchlab.squeaky.table.TableInfo;
import co.touchlab.squeaky.table.TableUtils;
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
	public static final String DEFAULT_TABLE_PREFIX = "t";
	public static final String EQ_OPERATION = "= ?";

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
			selectList[i] = DEFAULT_TABLE_PREFIX + "." + fieldTypes[i].getColumnName();
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
		return makeCursorResults(createDefaultFrom(), null, null, null);
	}

	public List<T> queryForEq(String fieldName, Object value) throws SQLException
	{
		return queryForEq(fieldName, value, null);
	}

	public List<T> queryForEq(String fieldName, Object value, String orderBy) throws SQLException
	{
		List<String> params = new ArrayList<>();
		Class<T> dataClass = generatedTableMapper.getTableConfig().dataClass;
		FieldType fieldType = openHelperHelper.findFieldType(dataClass, fieldName);

		SqlHelper.appendArgOrValue(openHelperHelper, fieldType, params, value);

		if(TextUtils.isEmpty(orderBy))
			orderBy = null;

		StringBuilder sb = new StringBuilder();
		SqlHelper.appendWhereClauseBody(sb, DEFAULT_TABLE_PREFIX, EQ_OPERATION, fieldType);
		return makeCursorResults(createDefaultFrom(), sb.toString(), params.toArray(new String[params.size()]), orderBy);
	}

	public List<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException
	{
		return queryForFieldValues(fieldValues, null);
	}

	public List<T> queryForFieldValues(Map<String, Object> fieldValues, String orderBy) throws SQLException
	{
		StringBuilder query = new StringBuilder();
		List<String> params = new ArrayList<>();

		for (String field : fieldValues.keySet())
		{
			if(query.length() > 0)
				query.append(" and ");

			Class<T> dataClass = generatedTableMapper.getTableConfig().dataClass;
			FieldType fieldType = openHelperHelper.findFieldType(dataClass, field);

			SqlHelper.appendWhereClauseBody(query, DEFAULT_TABLE_PREFIX, EQ_OPERATION, fieldType);
			SqlHelper.appendArgOrValue(openHelperHelper, fieldType, params, fieldValues.get(field));
		}

		if(TextUtils.isEmpty(orderBy))
			orderBy = null;

		return makeCursorResults(createDefaultFrom(), query.toString(), params.toArray(new String[params.size()]), orderBy);
	}

	private String createDefaultFrom() throws SQLException
	{
		StringBuilder sb = new StringBuilder();

		sb.append(generatedTableMapper.getTableConfig().getTableName());
		sb.append(' ');
		sb.append(DEFAULT_TABLE_PREFIX);

		return sb.toString();
	}

	private List<T> makeCursorResults(String from, String where, String[] args, String orderBy) throws SQLException
	{
		List<T> results = new ArrayList<T>();
		TransientCache objectCache = new TransientCache();
		Cursor cursor = makeCursor(from, where, args, orderBy);

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

	private Cursor makeCursor(String from, String where, String[] args, String orderBy)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(TextUtils.join(",", tableCols)).append(" from ").append(from);
		if(!TextUtils.isEmpty(where))
			sb.append(" where ").append(where);

		if(!TextUtils.isEmpty(orderBy))
			sb.append(" order by ").append(orderBy);

		String sql = sb.toString();
		return openHelperHelper.getHelper().getWritableDatabase()
				.rawQuery(sql, args);
	}

	public List<T> query(String where, String[] args, String orderBy) throws SQLException
	{
		return makeCursorResults(createDefaultFrom(), where, args, orderBy);
	}

	public List<T> query(String where, String[] args)throws SQLException
	{
		return query(where, args, null);
	}

	public List<T> query(Query where, String orderBy)throws SQLException
	{
		return makeCursorResults(where.getFromStatement(), where.getWhereStatement(), where.getParameters(), orderBy);
	}

	public List<T> query(Query where)throws SQLException
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
		Cursor cursor = makeCursor(createDefaultFrom(), idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()}, null);
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
		final StringBuilder sb = new StringBuilder();
		sb.append(idFieldType.getColumnName()).append(" in (");
		boolean first = true;
		for (ID id : ids)
		{
			if(first)
				first = false;
			else
				sb.append(',');
			if(idFieldType.isEscapedValue())
				TableUtils.appendEscapedWord(sb, id.toString());
			else
				sb.append(id.toString());
		}
		sb.append(")");
		return delete(new Query()
		{
			@Override
			public String getWhereStatement()
			{
				return sb.toString();
			}

			@Override
			public String[] getParameters() throws SQLException
			{
				return null;
			}

			@Override
			public String getFromStatement() throws SQLException
			{
				return createDefaultFrom();
			}
		});
	}

	public int delete(Query where) throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(where.getFromStatement());
		String whereStatement = where.getWhereStatement();
		if(!TextUtils.isEmpty(whereStatement))
			sb.append(" where ").append(whereStatement);

		SQLiteStatement sqLiteStatement = openHelperHelper.getHelper().getWritableDatabase().compileStatement(sb.toString());
		String[] parameters = where.getParameters();

		if(parameters != null && parameters.length > 0)
			sqLiteStatement.bindAllArgsAsStrings(parameters);

		int result = sqLiteStatement.executeUpdateDelete();

		notifyChanges();

		return result;
	}

	public CloseableIterator<T> iterator() throws SQLException
	{
		return new SelectIterator<T, ID>(
				makeCursor(createDefaultFrom(), null, null, null),
				ModelDao.this
		);
	}

	public CloseableIterator<T> iterator(Query where) throws SQLException
	{
		return new SelectIterator<T, ID>(
				makeCursor(where.getFromStatement(), where.getWhereStatement(), where.getParameters(), null),
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
		/*ForeignCollectionInfo foreignCollectionInfo = openHelperHelper.findForeignCollectionInfo(generatedTableMapper.getClass(), fieldName);

		Dao dao = openHelperHelper.getDao(foreignCollectionInfo.foreignFieldType);
		GeneratedTableMapper generatedTableMapper = openHelperHelper.getGeneratedTableMapper(foreignCollectionInfo.foreignFieldType);
		FieldType fieldType = openHelperHelper.findFieldType(foreignCollectionInfo.foreignFieldType, foreignCollectionInfo.variableName);

		List list = dao.queryForEq(fieldType.getColumnName(), extractId(data));
		System.out.println(foreignCollectionInfo.foreignFieldName);*/
		generatedTableMapper.fillForeignCollection(data, this, fieldName);
	}

	/*public List findForeignCollectionValues(final Object sourceId, final FieldType foreignIdField)throws SQLException
	{
		return query(new Query()
		{
			@Override
			public String getWhereStatement() throws SQLException
			{
				StringBuilder sb = new StringBuilder();
				sb.append(foreignIdField.getColumnName());
				sb.append(" = ");

				if(foreignIdField.getDataPersister().isEscapedValue())
					TableUtils.appendEscapedWord(sb, sourceId.toString());
				else
					sb.append(sourceId.toString());
				return sb.toString();
			}
		});
	}*/

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

	public long countOf(Query where) throws SQLException
	{
		return DatabaseUtils.longForQuery(openHelperHelper.getHelper().getWritableDatabase(), "select count(*) from "+ where.getFromStatement() +" where "+ where.getWhereStatement(), where.getParameters());
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

	public ForeignCollectionInfo findForeignCollectionInfo(String name) throws SQLException
	{
		for (ForeignCollectionInfo foreignCollectionInfo : generatedTableMapper.getTableConfig().getForeignCollections())
		{
			if(name.equals(foreignCollectionInfo.variableName))
				return foreignCollectionInfo;
		}

		throw new SQLException("Couldn't find foreign collection children");
	}

}
