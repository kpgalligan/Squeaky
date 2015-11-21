package co.touchlab.squeaky.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import co.touchlab.squeaky.Config;
import co.touchlab.squeaky.db.SQLiteStatement;
import co.touchlab.squeaky.db.SQLiteDatabase;
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
public class ModelDao<T> implements Dao<T>
{
	public static final String DEFAULT_TABLE_PREFIX = "t";
	public static final String EQ_OPERATION = "= ?";

	private final Class<T> entityClass;
	private final GeneratedTableMapper<T> generatedTableMapper;
	private final Set<DaoObserver> daoObserverSet = Collections.newSetFromMap(new ConcurrentHashMap<DaoObserver, Boolean>());
	private final String[] tableCols;
	private final SqueakyContext squeakyContext;
	private final FieldType idFieldType;
	private final List<SQLiteStatement> statementList = Collections.synchronizedList(new ArrayList<SQLiteStatement>());
	private ThreadLocal<SQLiteStatement> createStatement = new ThreadLocal<SQLiteStatement>(){
		@Override
		protected SQLiteStatement initialValue()
		{
			SQLiteStatement sqLiteStatement = makeCreateStatement();
			statementList.add(sqLiteStatement);
			return sqLiteStatement;
		}
	};
	private ThreadLocal<SQLiteStatement> updateStatement = new ThreadLocal<SQLiteStatement>(){
		@Override
		protected SQLiteStatement initialValue()
		{
			SQLiteStatement sqLiteStatement = makeUpdateStatement();
			statementList.add(sqLiteStatement);
			return sqLiteStatement;
		}
	};

	protected ModelDao(SqueakyContext openHelper, Class<T> entityClass, GeneratedTableMapper<T> generatedTableMapper)
	{
		this.squeakyContext = openHelper;
		this.entityClass = entityClass;
		try
		{
			this.generatedTableMapper = generatedTableMapper;

			FieldType idField = null;
			FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
			for (FieldType fieldType : fieldTypes)
			{
				if (fieldType.isId() || fieldType.isGeneratedId())
				{
					idField = fieldType;
					break;
				}
			}

			idFieldType = idField;

			tableCols = buildSelect();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void cleanUp()
	{
		for (SQLiteStatement sqLiteStatement : statementList)
		{
			sqLiteStatement.close();
		}
	}

	private String[] buildSelect() throws SQLException
	{
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		String[] selectList = new String[fieldTypes.length];

		for (int i = 0; i < fieldTypes.length; i++)
		{
			selectList[i] = DEFAULT_TABLE_PREFIX + "." + fieldTypes[i].getColumnName();
		}
		return selectList;
	}

	@Override
	public T queryForId(Object id) throws SQLException
	{
		List<T> tList = queryForEq(idFieldType.getColumnName(), id).list();
		return tList.size() == 0 ? null : tList.get(0);
	}

	@Override
	public QueryModifiers<T> queryForAll() throws SQLException
	{
		return new QueryModifiersImpl(createDefaultFrom(), null, null);
	}

	@Override
	public QueryModifiers<T> queryForEq(String fieldName, Object value) throws SQLException
	{
		List<String> params = new ArrayList<>();
		Class<T> dataClass = generatedTableMapper.getTableConfig().dataClass;
		FieldType fieldType = squeakyContext.findFieldType(dataClass, fieldName);

		SqlHelper.appendArgOrValue(squeakyContext, fieldType, params, value);

		StringBuilder sb = new StringBuilder();
		SqlHelper.appendWhereClauseBody(sb, DEFAULT_TABLE_PREFIX, EQ_OPERATION, fieldType);
		return new QueryModifiersImpl(createDefaultFrom(), sb.toString(), params.toArray(new String[params.size()]));
	}

	@Override
	public QueryModifiers<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException
	{
		StringBuilder query = new StringBuilder();
		List<String> params = new ArrayList<>();

		for (String field : fieldValues.keySet())
		{
			if (query.length() > 0)
				query.append(" and ");

			Class<T> dataClass = generatedTableMapper.getTableConfig().dataClass;
			FieldType fieldType = squeakyContext.findFieldType(dataClass, field);

			SqlHelper.appendWhereClauseBody(query, DEFAULT_TABLE_PREFIX, EQ_OPERATION, fieldType);
			SqlHelper.appendArgOrValue(squeakyContext, fieldType, params, fieldValues.get(field));
		}

		return new QueryModifiersImpl(createDefaultFrom(), query.toString(), params.toArray(new String[params.size()]));
	}

	class QueryModifiersImpl implements QueryModifiers<T>
	{
		private final String from;
		private final String where;
		private final String[] args;
		private String orderBy;
		private Integer limit;
		private Integer offset;
		private ForeignRefresh[] foreignRefreshMap;

		public QueryModifiersImpl(String from, String where, String[] args)
		{
			this.from = from;
			this.where = where;
			this.args = args;
		}

		@Override
		public QueryModifiers<T> orderBy(String s)
		{
			orderBy = s;
			return this;
		}

		@Override
		public QueryModifiers<T> limit(Integer i)
		{
			limit = i;
			return this;
		}

		@Override
		public QueryModifiers<T> offset(Integer i)
		{
			offset = i;
			return this;
		}

		@Override
		public QueryModifiers<T> foreignRefreshMap(ForeignRefresh[] foreignRefreshMap)
		{
			this.foreignRefreshMap = foreignRefreshMap;
			return this;
		}

		@Override
		public List<T> list() throws SQLException
		{
			return makeCursorResults(from, where, args, orderBy, limit, offset, foreignRefreshMap == null ? generateDefaultForeignRefreshMap() : foreignRefreshMap);
		}
	}

	private String createDefaultFrom() throws SQLException
	{
		StringBuilder sb = new StringBuilder();

		sb.append(generatedTableMapper.getTableConfig().getTableName());
		sb.append(' ');
		sb.append(DEFAULT_TABLE_PREFIX);

		return sb.toString();
	}

	private List<T> makeCursorResults(String from, String where, String[] args, String orderBy, Integer limit, Integer offset, ForeignRefresh[] foreignRefreshMap) throws SQLException
	{
		List<T> results = new ArrayList<T>();
		TransientCache objectCache = new TransientCache();
		Cursor cursor = makeCursor(from, where, args, orderBy, limit, offset);

		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					T object = generatedTableMapper.createObject(cursor);
					generatedTableMapper.fillRow(object, cursor, this, foreignRefreshMap, objectCache);
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

	private Cursor makeCursor(String from, String where, String[] args, String orderBy, Integer limit, Integer offset)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(TextUtils.join(",", tableCols)).append(" from ").append(from);
		if (!TextUtils.isEmpty(where))
			sb.append(" where ").append(where);

		if (!TextUtils.isEmpty(orderBy))
			sb.append(" order by ").append(orderBy);

		if (limit != null)
			sb.append(" limit ").append(limit);

		if (offset != null)
			sb.append(" offset ").append(offset);

		String sql = sb.toString();
		return squeakyContext.getDatabase()
				.rawQuery(sql, args);
	}

	@Override
	public QueryModifiers<T> query(String where, String[] args) throws SQLException
	{
		return new QueryModifiersImpl(createDefaultFrom(), where, args);
	}

	@Override
	public QueryModifiers<T> query(Query where) throws SQLException
	{
		return new QueryModifiersImpl(where.getFromStatement(true), where.getWhereStatement(true), where.getParameters());
	}

	@Override
	public void create(T data) throws SQLException
	{
		SQLiteStatement sqLiteStatement = createStatement.get();

		generatedTableMapper.bindCreateVals(sqLiteStatement, data);

		try
		{
			long newRowId = sqLiteStatement.executeInsert();

			if (idFieldType != null && idFieldType.isGeneratedId())
			{
				generatedTableMapper.assignId(data, newRowId);
			}

			notifyChanges();
		}
		catch (SQLiteException e)
		{
			throw new SQLException("create failed", e);
		}
	}

	private SQLiteStatement makeCreateStatement()
	{
		try
		{
			SQLiteDatabase db = squeakyContext.getDatabase();
			TableInfo<T> tableConfig = generatedTableMapper.getTableConfig();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into ");
			sb.append(tableConfig.getTableName());
			sb.append("(");
			boolean first = true;
			StringBuilder args = new StringBuilder();
			for (FieldType fieldType : generatedTableMapper.getTableConfig().getFieldTypes())
			{
				if (!fieldType.isGeneratedId())
				{
					if (!first)
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

			return db.compileStatement(sb.toString());
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void fillContentVal(ContentValues values, FieldType fieldType, Object val)
	{
		if (val instanceof String)
			values.put(fieldType.getColumnName(), (String) val);
		else if (val instanceof Integer)
			values.put(fieldType.getColumnName(), (Integer) val);
		else if (val instanceof Long)
			values.put(fieldType.getColumnName(), (Long) val);
		else if (val instanceof Byte)
			values.put(fieldType.getColumnName(), (Byte) val);
		else if (val instanceof Short)
			values.put(fieldType.getColumnName(), (Short) val);
		else if (val instanceof Float)
			values.put(fieldType.getColumnName(), (Float) val);
		else if (val instanceof Double)
			values.put(fieldType.getColumnName(), (Double) val);
		else if (val instanceof Boolean)
			values.put(fieldType.getColumnName(), (Boolean) val);
		else if (val instanceof byte[])
			values.put(fieldType.getColumnName(), (byte[]) val);
		else
			throw new IllegalArgumentException("Don't recognize type for: " + val);
	}

	@Override
	public T createIfNotExists(T data) throws SQLException
	{
		if (data == null)
		{
			return null;
		}
		T existing = queryForId(generatedTableMapper.extractId(data));
		if (existing == null)
		{
			create(data);
			return data;
		}
		else
		{
			return existing;
		}
	}

	@Override
	public void createOrUpdate(T data) throws SQLException
	{
		Object id = extractId(data);
		// assume we need to create it if there is no id
		if (id == null || !idExists(id))
		{
			create(data);
		}
		else
		{
			update(data);
		}
	}

	@Override
	public void update(T data) throws SQLException
	{
		SQLiteStatement us = updateStatement.get();
		generatedTableMapper.bindVals(us, data);

		us.executeUpdateDelete();

		notifyChanges();
	}

	private SQLiteStatement makeUpdateStatement()
	{
		try
		{
			SQLiteDatabase db = squeakyContext.getDatabase();
			TableInfo<T> tableConfig = generatedTableMapper.getTableConfig();
			StringBuilder sb = new StringBuilder();
			sb.append("update ").append(tableConfig.getTableName()).append(" set ");
			boolean first = true;

			for (FieldType fieldType : generatedTableMapper.getTableConfig().getFieldTypes())
			{
				if (!fieldType.isGeneratedId() && !fieldType.isId())
				{
					if (!first)
					{
						sb.append(",");
					}
					sb.append(fieldType.getColumnName()).append(" = ?");
					first = false;
				}
			}

			sb.append(" where ").append(generatedTableMapper.getTableConfig().idField.getColumnName()).append(" = ?");

			return db.compileStatement(sb.toString());
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public int updateId(T data, Object newId) throws SQLException
	{
		SQLiteDatabase db = squeakyContext.getDatabase();

		ContentValues values = new ContentValues();

		fillContentVal(values, idFieldType, newId);

		int result = db.update(generatedTableMapper.getTableConfig().getTableName(), values, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()});

		notifyChanges();

		return result;
	}

	@Override
	public int update(Query where, Map<String, Object> valueMap) throws SQLException
	{
		SQLiteDatabase db = squeakyContext.getDatabase();
		ContentValues values = new ContentValues();

		for (String fieldKey : valueMap.keySet())
		{
			FieldType fieldType = squeakyContext.findFieldType(generatedTableMapper.getTableConfig().dataClass, fieldKey);
			fillContentVal(values, fieldType,
					SqlHelper.pullArgOrValue(squeakyContext, fieldType, valueMap.get(fieldKey)));
		}

		int result = db.update(
				generatedTableMapper.getTableConfig().getTableName(),
				values,
				where.getWhereStatement(false),
				where.getParameters()
		);

		notifyChanges();

		return result;
	}

	@Override
	public void refresh(T data) throws SQLException
	{
		refresh(data, generateDefaultForeignRefreshMap());
	}

	@Override
	public void refresh(T data, Dao.ForeignRefresh[] foreignRefreshMap) throws SQLException
	{
		Cursor cursor = makeCursor(createDefaultFrom(), idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()}, null, null, null);
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					generatedTableMapper.fillRow(data, cursor, this, foreignRefreshMap, null);
				} while (cursor.moveToNext());
			}
		}
		finally
		{
			cursor.close();
		}
	}

	@Override
	public int delete(T data) throws SQLException
	{
		return data == null ? 0 : deleteById(generatedTableMapper.extractId(data));
	}

	@Override
	public int deleteById(Object id) throws SQLException
	{
		int result = squeakyContext.getDatabase().delete(
				generatedTableMapper.getTableConfig().getTableName(),
				idFieldType.getColumnName() + "= ?",
				new String[]{id.toString()});

		notifyChanges();

		return result;
	}

	@Override
	public int delete(Collection<T> datas) throws SQLException
	{
		List ids = new ArrayList(datas.size());
		for (T data : datas)
		{
			ids.add(generatedTableMapper.extractId(data));
		}

		return deleteIds(ids);
	}

	@Override
	public int deleteIds(Collection<Object> ids) throws SQLException
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(idFieldType.getColumnName()).append(" in (");

		boolean first = true;
		for (Object id : ids)
		{
			if (first)
				first = false;
			else
				sb.append(',');
			if (idFieldType.isEscapedValue())
				TableUtils.appendEscapedWord(sb, id.toString());
			else
				sb.append(id.toString());
		}
		sb.append(")");

		return delete(new Query()
		{
			@Override
			public String getWhereStatement(boolean joinsAllowed)
			{
				return sb.toString();
			}

			@Override
			public String[] getParameters() throws SQLException
			{
				return null;
			}

			@Override
			public String getFromStatement(boolean joinsAllowed) throws SQLException
			{
				return generatedTableMapper.getTableConfig().getTableName();
			}
		});
	}

	@Override
	public int delete(Query where) throws SQLException
	{
		long result = squeakyContext.getDatabase().delete(
				where.getFromStatement(false),
				where.getWhereStatement(false),
				where.getParameters());

		notifyChanges();

		return (int)result;
	}

	@Override
	public CloseableIterator<T> iterator() throws SQLException
	{
		return new SelectIterator<T>(
				makeCursor(createDefaultFrom(), null, null, null, null, null),
				ModelDao.this,
				generateDefaultForeignRefreshMap());
	}

	private ForeignRefresh[] generateDefaultForeignRefreshMap() throws SQLException
	{
		return DaoHelper.fillForeignRefreshMap(squeakyContext, generatedTableMapper.getTableConfig().getFieldTypes(), Config.MAX_AUTO_REFRESH);
	}

	@Override
	public CloseableIterator<T> iterator(Query where) throws SQLException
	{
		return new SelectIterator<T>(
				makeCursor(where.getFromStatement(true), where.getWhereStatement(true), where.getParameters(), null, null, null),
				ModelDao.this,
				generateDefaultForeignRefreshMap());
	}

	@Override
	public long queryRawValue(String query, String... arguments) throws SQLException
	{
		return squeakyContext.getDatabase().longForQuery(query, arguments);
//		return DatabaseUtils.longForQuery(squeakyContext.getDatabase(), query, arguments);
	}

	@Override
	public String objectToString(T data) throws SQLException
	{
		return generatedTableMapper.objectToString(data);
	}

	@Override
	public boolean objectsEqual(T data1, T data2) throws SQLException
	{
		return generatedTableMapper.objectsEqual(data1, data2);
	}

	@Override
	public Object extractId(T data) throws SQLException
	{
		return generatedTableMapper.extractId(data);
	}

	@Override
	public void fillForeignCollection(T data, String fieldName) throws SQLException
	{
		generatedTableMapper.fillForeignCollection(data, this, fieldName);
	}

	@Override
	public Class<T> getDataClass()
	{
		return entityClass;
	}

	@Override
	public boolean isUpdatable()
	{
		return true;
	}

	@Override
	public long countOf() throws SQLException
	{
		return squeakyContext.getDatabase().countOf(generatedTableMapper.getTableConfig().getTableName());
//		return DatabaseUtils.queryNumEntries(squeakyContext.getHelper().getWritableDatabase(), generatedTableMapper.getTableConfig().getTableName());
	}

	@Override
	public long countOf(Query where) throws SQLException
	{
		return squeakyContext.getDatabase().countOf(where);
//		return DatabaseUtils.longForQuery(squeakyContext.getHelper().getWritableDatabase(), "select count(*) from " + where.getFromStatement(true) + " where " + where.getWhereStatement(true), where.getParameters());
	}

	@Override
	//TODO could be faster
	public boolean idExists(Object id) throws SQLException
	{
		return queryForId(id) != null;
	}

	@Override
	public void registerObserver(DaoObserver observer)
	{
		daoObserverSet.add(observer);
	}

	@Override
	public void unregisterObserver(DaoObserver observer)
	{
		daoObserverSet.remove(observer);
	}

	@Override
	public void notifyChanges()
	{
		for (DaoObserver next : daoObserverSet)
		{
			next.onChange();
		}
	}

	@Override
	public Query all()
	{
		return new Query()
		{
			@Override
			public String getFromStatement(boolean joinsAllowed) throws SQLException
			{
				StringBuilder sb = new StringBuilder();
				sb.append(generatedTableMapper.getTableConfig().getTableName());
				if(joinsAllowed)
					sb.append(" ").append(DEFAULT_TABLE_PREFIX);
				return  sb.toString();
			}

			@Override
			public String getWhereStatement(boolean joinsAllowed) throws SQLException
			{
				return null;
			}

			@Override
			public String[] getParameters() throws SQLException
			{
				return null;
			}
		};
	}

	public GeneratedTableMapper<T> getGeneratedTableMapper()
	{
		return generatedTableMapper;
	}

	public SqueakyContext getOpenHelper()
	{
		return squeakyContext;
	}

	public ForeignCollectionInfo findForeignCollectionInfo(String name) throws SQLException
	{
		for (ForeignCollectionInfo foreignCollectionInfo : generatedTableMapper.getTableConfig().getForeignCollections())
		{
			if (name.equals(foreignCollectionInfo.variableName))
				return foreignCollectionInfo;
		}

		throw new SQLException("Couldn't find foreign collection children");
	}
}
