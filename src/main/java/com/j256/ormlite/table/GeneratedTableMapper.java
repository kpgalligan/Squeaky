package com.j256.ormlite.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.j256.ormlite.android.squeaky.ModelDao;

import java.sql.SQLException;

/**
 * Created by kgalligan on 5/24/15.
 */
public interface GeneratedTableMapper<T, ID>
{
	T createObject();
	void fillRow(T data, Cursor results, ModelDao<T, ID> modelDao, Integer recursiveAutorefreshCountdown) throws SQLException;
	void assignId(T data, Object val);
	ID extractId(T data);
	void bindVals(SQLiteStatement stmt, T data) throws SQLException;
	void bindCreateVals(SQLiteStatement stmt, T data) throws SQLException;
	String objectToString(T data)throws SQLException;
	boolean objectsEqual(T d1, T d2)throws SQLException;
	TableInfo<T, ID> getTableConfig()throws SQLException;

	//Foreign

}
