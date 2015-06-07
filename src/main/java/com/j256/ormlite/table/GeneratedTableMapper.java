package com.j256.ormlite.table;

import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by kgalligan on 5/24/15.
 */
public interface GeneratedTableMapper<T, ID>
{
	T createObject();
	void fillRow(T data, DatabaseResults results) throws SQLException;
	void assignVersion(T data, Object val);
	void assignId(T data, Object val);
	ID extractId(T data);
	Object extractVersion(T data);
	Object[] extractVals(T data)throws SQLException;
	Object[] extractCreateVals(T data)throws SQLException;

	//Foreign

}
