package com.j256.ormlite.field.types;

import android.database.Cursor;
import com.j256.ormlite.field.BaseFieldConverter;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Base data type that defines the default persistance methods for the various data types.
 * 
 * <p>
 * Here's a good page about the <a href="http://docs.codehaus.org/display/CASTOR/Type+Mapping" >mapping for a number of
 * database types</a>:
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> If you are creating your own custom database persister, you probably will need to override the
 * {@link BaseFieldConverter#sqlArgToJava(FieldType, Object, int)} method as well which converts from a SQL data to
 * java.
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseDataType extends BaseFieldConverter implements DataPersister {

	private final static Class<?>[] NO_CLASSES = new Class<?>[0];

	/**
	 * Type of the data as it is persisted in SQL-land. For example, if you are storing a DateTime, you might consider
	 * this to be a {@link SqlType#LONG} if you are storing it as epoche milliseconds.
	 */
	private final SqlType sqlType;
	private final Class<?>[] classes;

	/**
	 * @param sqlType
	 *            Type of the class as it is persisted in the databases.
	 * @param classes
	 *            Associated classes for this type. These should be specified if you want this type to be always used
	 *            for these Java classes. If this is a custom persister then this array should be empty.
	 */
	public BaseDataType(SqlType sqlType, Class<?>[] classes) {
		this.sqlType = sqlType;
		this.classes = classes;
	}

	/**
	 * @param sqlType
	 *            Type of the class as it is persisted in the databases.
	 */
	public BaseDataType(SqlType sqlType) {
		this.sqlType = sqlType;
		this.classes = NO_CLASSES;
	}

	public abstract Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

	public abstract Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos)
			throws SQLException;

	public Object resultToJava(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return sqlArgToJava(fieldType, resultToSqlArg(fieldType, results, columnPos), columnPos);
	}

	public Class<?> getPrimaryClass() {
		if (classes.length == 0) {
			return null;
		} else {
			return classes[0];
		}
	}

	/**
	 * @throws SQLException
	 *             If there are problems creating the config object. Needed for subclasses.
	 */
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		return null;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public Class<?>[] getAssociatedClasses() {
		return classes;
	}

	public String[] getAssociatedClassNames() {
		return null;
	}

	public boolean isEscapedDefaultValue() {
		// default is to not escape the type if it is a number
		return isEscapedValue();
	}

	public boolean isEscapedValue() {
		return true;
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isComparable() {
		return true;
	}

	public boolean isArgumentHolderRequired() {
		return false;
	}

	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		return sqlArgToJava(fieldType, parseDefaultString(fieldType, stringValue), columnPos);
	}
}
