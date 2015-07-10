package com.j256.ormlite.field;

import android.database.Cursor;

import java.sql.SQLException;

/**
 * Convert a Java object into the appropriate argument to a SQL statement and then back from the result set to the Java
 * object. This allows databases to configure per-type conversion. This is used by the
 * {@link BaseDatabaseType#getFieldConverter(DataPersister, FieldType)} method to find the converter for a particular
 * database type. Databases can then override the default data conversion mechanisms as necessary.
 * 
 * @author graywatson
 */
public interface FieldConverter {

	/**
	 * Convert a default string object and return the appropriate argument to a SQL insert or update statement.
	 */
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

	/**
	 * Convert a Java object and return the appropriate argument to a SQL insert or update statement.
	 */
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException;

	/**
	 * Return the SQL argument object extracted from the results associated with column in position columnPos. For
	 * example, if the type is a date-long then this will return a long value or null.
	 * 
	 * @throws SQLException
	 *             If there is a problem accessing the results data.
	 * @param fieldType
	 *            Associated FieldType which may be null.
	 */
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException;

	public Object resultToJava(FieldType fieldType, Cursor results, int columnPos)throws SQLException;

	/**
	 * Return the object converted from the SQL arg to java. This takes the database representation and converts it into
	 * a Java object. For example, if the type is a date-long then this will take a long which is stored in the database
	 * and return a Date.
	 * 
	 * @param fieldType
	 *            Associated FieldType which may be null.
	 * @param sqlArg
	 *            SQL argument converted with {@link #resultToSqlArg(FieldType, DatabaseResults, int)} which will not be
	 *            null.
	 */
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException;

	/**
	 * Return the SQL type that is stored in the database for this argument.
	 */
	public SqlType getSqlType();

	/**
	 * Convert a string result value to the related Java field.
	 */
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException;
}
