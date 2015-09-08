package co.touchlab.squeaky.dao;

import android.database.sqlite.SQLiteOpenHelper;
import co.touchlab.squeaky.table.GeneratedTableMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Logic for managing access to Dao instances.  If you don't want to extend SqueakyOpenHelper you can use this directly.
 * @author kgalligan
 */
public class SqueakyContext
{
	private final Class[] managingClasses;
	private final SQLiteOpenHelper helper;
	private final Map<Class, ModelDao> daoMap = new HashMap<Class, ModelDao>();
	private final Map<Class, GeneratedTableMapper> generatedTableMapperMap = new HashMap<Class, GeneratedTableMapper>();


	public SqueakyContext(SQLiteOpenHelper helper, Class[] managingClasses)
	{
		this.helper = helper;
		this.managingClasses = managingClasses;
	}

	public synchronized Dao getDao(Class clazz)
	{
		ModelDao dao = daoMap.get(clazz);
		if(dao == null)
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
			((ModelDao)dao).cleanUp();
		}
		daoMap.clear();
	}

	public synchronized GeneratedTableMapper getGeneratedTableMapper(Class clazz)
	{
		GeneratedTableMapper generatedTableMapper = generatedTableMapperMap.get(clazz);
		if(generatedTableMapper == null)
		{
			generatedTableMapper = SqueakyOpenHelper.loadGeneratedTableMapper(clazz);
			generatedTableMapperMap.put(clazz, generatedTableMapper);
		}

		return generatedTableMapper;
	}

	public Class[] getManagingClasses()
	{
		return managingClasses;
	}

	public SQLiteOpenHelper getHelper()
	{
		return helper;
	}
}
