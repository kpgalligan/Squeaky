package com.j256.ormlite.android.squeaky;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kgalligan on 6/15/15.
 */
public abstract class SqueakyOpenHelper extends SQLiteOpenHelper
{
	private final Class[] managingClasses;

	private final Map<Class, Dao> daoMap = new HashMap<Class, Dao>();

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
			dao = new ModelDao(clazz);
			daoMap.put(clazz, dao);
		}

		return dao;
	}
}
