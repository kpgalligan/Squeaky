package com.j256.ormlite.android.squeaky;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.GeneratedTableMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kgalligan on 6/15/15.
 */
public class ModelDao<T, ID> implements Dao<T, ID>
{
	private final Class<T> entityClass;
	private final GeneratedTableMapper<T, ID> generatedTableMapper;
	private final Set<DaoObserver> daoObserverSet = Collections.newSetFromMap(new ConcurrentHashMap<DaoObserver, Boolean>());
	private final String[] tableCols;
	private final SqueakyOpenHelper openHelper;
	private final FieldType idFieldType;

	protected ModelDao(SqueakyOpenHelper openHelper, Class<T> entityClass, GeneratedTableMapper<T, ID> generatedTableMapper)
	{
		this.openHelper = openHelper;
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

			if(idField == null)
				throw new IllegalStateException("Must have an id field");

			idFieldType = idField;

			tableCols = buildSelect();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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
		return makeCursorResults(null, null);
	}

	public List<T> queryForEq(String fieldName, Object value) throws SQLException
	{
		return makeCursorResults(fieldName + " = ?", new String[]{value.toString()});
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

		return makeCursorResults(query.toString(), args);
	}

	public Where<T, ID> createWhere() throws SQLException
	{
		return new Where<T, ID>(generatedTableMapper);
	}

	private List<T> makeCursorResults(String where, String[] args) throws SQLException
	{
		List<T> results = new ArrayList<T>();
		Cursor cursor = openHelper.getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, where, args, null, null, null);
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					T object = generatedTableMapper.createObject();
					generatedTableMapper.fillRow(object, cursor);
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

	public List<T> query(String where) throws SQLException
	{
		return makeCursorResults(where, null);
	}

	public List<T> query(Where<T, ID> where)throws SQLException
	{
		String statement = where.getStatement();
		return query(statement);
	}

	public void create(T data) throws SQLException
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = fillContentValues(data, false);

		long newRowId = db.insertOrThrow(generatedTableMapper.getTableConfig().getTableName(), null, values);

		if(idFieldType.isGeneratedId())
		{
			generatedTableMapper.assignId(data, newRowId);
		}
	}

	private ContentValues fillContentValues(T data, boolean updating) throws SQLException
	{
		ContentValues values = new ContentValues();
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		Object[] vals = generatedTableMapper.extractVals(data);
		int count =0;
		for (FieldType fieldType : fieldTypes)
		{
			Object val = vals[count++];
			if(fieldType.isGeneratedId())
				continue;

			if(updating && fieldType.isId())
				continue;

			if(val == null)
				values.putNull(fieldType.getColumnName());
			else
				fillContentVal(values, fieldType, val);
		}
		return values;
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
		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = fillContentValues(data, true);

		db.update(generatedTableMapper.getTableConfig().getTableName(), values, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()});
	}

	public int updateId(T data, ID newId) throws SQLException
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = new ContentValues();

		fillContentVal(values, idFieldType, newId);

		return db.update(generatedTableMapper.getTableConfig().getTableName(), values, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()});
	}

	public void refresh(T data) throws SQLException
	{
		Cursor cursor = openHelper.getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, idFieldType.getColumnName() + " = ?", new String[]{generatedTableMapper.extractId(data).toString()}, null, null, null);
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					generatedTableMapper.fillRow(data, cursor);
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
		return openHelper.getWritableDatabase().delete(generatedTableMapper.getTableConfig().getTableName(), idFieldType.getColumnName() + "= ?", new String[]{id.toString()});
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
		int count = 0;
		for (ID id : ids)
		{
			count += deleteById(id);
		}

		return count;
	}

	public int delete(Where<T, ID> where) throws SQLException
	{
		throw new UnsupportedOperationException("need where");
	}

	public CloseableIterator<T> iterator() throws SQLException
	{
		return new SelectIterator<T, ID>(
				openHelper.getWritableDatabase().query(generatedTableMapper.getTableConfig().getTableName(), tableCols, null, null, null, null, null),
				generatedTableMapper
		);
	}

	public CloseableIterator<T> iterator(Where<T, ID> where) throws SQLException
	{
		throw new RuntimeException("need where");
	}

	public long queryRawValue(String query, String... arguments) throws SQLException
	{
		throw new UnsupportedOperationException("queryRawValue");
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
		return queryRawValue("select count(*) from "+ generatedTableMapper.getTableConfig().getTableName());
	}

	public long countOf(Where<T, ID> where) throws SQLException
	{
		throw new UnsupportedOperationException("Just where");
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


}
