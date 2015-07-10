package com.j256.ormlite.field.types;

import android.database.Cursor;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a String object.
 * 
 * @author graywatson
 */
public class StringType extends BaseDataType {

	public static int DEFAULT_WIDTH = 255;

	private static final StringType singleTon = new StringType();

	public static StringType getSingleton() {
		return singleTon;
	}

	private StringType() {
		super(SqlType.STRING, new Class<?>[] { String.class });
	}

	protected StringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected StringType(SqlType sqlType) {
		super(sqlType);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return defaultStr;
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}
}
