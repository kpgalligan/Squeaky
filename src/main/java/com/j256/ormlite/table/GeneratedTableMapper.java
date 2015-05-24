package com.j256.ormlite.table;

import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by kgalligan on 5/24/15.
 */
public interface GeneratedTableMapper<T>
{
	T fillRow(DatabaseResults results, Map<String, Integer> columnPositions) throws SQLException;
}
