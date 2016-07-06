package co.touchlab.squeaky.dao;

import co.touchlab.squeaky.db.SQLiteDatabase;
import co.touchlab.squeaky.db.SQLiteOpenHelper;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.table.GeneratedTableMapper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Logic for managing access to Dao instances.  If you don't want to extend SqueakyOpenHelper you can use this directly.
 *
 * @author kgalligan
 */
public class SqueakyContext
{
	private final SQLiteOpenHelper helper;
	private final Map<Class, ModelDao> daoMap = new HashMap<Class, ModelDao>();
	private final Map<Class, GeneratedTableMapper> generatedTableMapperMap = new HashMap<Class, GeneratedTableMapper>();


	public SqueakyContext(SQLiteOpenHelper helper)
	{
		this.helper = helper;
	}

	public synchronized Dao getDao(Class clazz)
	{
		ModelDao dao = daoMap.get(clazz);
		if (dao == null)
		{
			dao = new ModelDao(this, clazz, getGeneratedTableMapper(clazz));
			daoMap.put(clazz, dao);
		}

		return dao;
	}

	public synchronized void close()
	{
		for (Dao dao : daoMap.values())
		{
			((ModelDao) dao).cleanUp();
		}
		daoMap.clear();
	}

	public synchronized GeneratedTableMapper getGeneratedTableMapper(Class clazz)
	{
		GeneratedTableMapper generatedTableMapper = generatedTableMapperMap.get(clazz);
		if (generatedTableMapper == null)
		{
			generatedTableMapper = loadGeneratedTableMapper(clazz);
			generatedTableMapperMap.put(clazz, generatedTableMapper);
		}

		return generatedTableMapper;
	}

	public SQLiteDatabase getDatabase()
	{
		return helper.getWrappedDatabase();
	}

	public FieldType findFieldType(Class c, String columnFieldName) throws SQLException
	{
		GeneratedTableMapper generatedTableMapper = getGeneratedTableMapper(c);
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		for (FieldType fieldType : fieldTypes)
		{
			if (fieldType.getFieldName().equalsIgnoreCase(columnFieldName) || fieldType.getColumnName().equalsIgnoreCase(columnFieldName))
			{
				return fieldType;
			}
		}

		throw new SQLException("No field type found for " + columnFieldName);
	}

	public static GeneratedTableMapper loadGeneratedTableMapper(Class clazz)
	{
		try
		{
			return (GeneratedTableMapper) Class.forName(clazz.getName() + "$Configuration").newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
