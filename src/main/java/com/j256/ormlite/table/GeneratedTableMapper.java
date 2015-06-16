package com.j256.ormlite.table;

import android.database.Cursor;

import java.sql.SQLException;

/**
 * Created by kgalligan on 5/24/15.
 */
public interface GeneratedTableMapper<T, ID>
{
	T createObject();
	void fillRow(T data, Cursor results) throws SQLException;
	void assignVersion(T data, Object val);
	void assignId(T data, Object val);
	ID extractId(T data);
	Object extractVersion(T data);
	Object[] extractVals(T data)throws SQLException;
	Object[] extractCreateVals(T data)throws SQLException;
	String objectToString(T data)throws SQLException;
	boolean objectsEqual(T d1, T d2)throws SQLException;
	TableInfo<T, ID> getTableConfig()throws SQLException;

	//Foreign

}
