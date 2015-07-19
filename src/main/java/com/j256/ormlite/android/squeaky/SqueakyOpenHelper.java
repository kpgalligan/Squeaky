package com.j256.ormlite.android.squeaky;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.j256.ormlite.table.GeneratedTableMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kgalligan on 6/15/15.
 */
public abstract class SqueakyOpenHelper extends SQLiteOpenHelper
{
	private final Class[] managingClasses;

	private final Map<Class, Dao> daoMap = new HashMap<Class, Dao>();
	private final Map<Class, GeneratedTableMapper> generatedTableMapperMap = new HashMap<Class, GeneratedTableMapper>();

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Class[] managingClasses)
	{
		super(context, name, factory, version);
		this.managingClasses = managingClasses;
	}

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler, Class[] managingClasses)
	{
		super(context, name, factory, version, errorHandler);
		this.managingClasses = managingClasses;
	}

	public synchronized Dao getDao(Class clazz)
	{
		Dao dao = daoMap.get(clazz);
		if(dao == null)
		{
			dao = new ModelDao(this, clazz, getGeneratedTableMapper(clazz));
			daoMap.put(clazz, dao);
		}

		return dao;
	}

	@Override
	public synchronized void close()
	{
		for (Dao dao : daoMap.values())
		{
			((ModelDao)dao).cleanUp();
		}
		daoMap.clear();
		super.close();
	}

	public synchronized GeneratedTableMapper getGeneratedTableMapper(Class clazz)
	{
		GeneratedTableMapper generatedTableMapper = generatedTableMapperMap.get(clazz);
		if(generatedTableMapper == null)
		{
			try
			{
				generatedTableMapper = (GeneratedTableMapper) Class.forName(clazz.getName() + "$$Configuration").newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			generatedTableMapperMap.put(clazz, generatedTableMapper);
		}

		return generatedTableMapper;
	}
}
