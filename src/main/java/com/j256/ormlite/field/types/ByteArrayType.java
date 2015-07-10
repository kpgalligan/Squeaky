package com.j256.ormlite.field.types;

import android.database.Cursor;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Type that persists a byte[] object.
 * 
 * @author graywatson
 */
public class ByteArrayType extends BaseDataType {

	private static final ByteArrayType singleTon = new ByteArrayType();

	public static ByteArrayType getSingleton() {
		return singleTon;
	}

	private ByteArrayType() {
		super(SqlType.BYTE_ARRAY);
	}

	/**
	 * Here for others to subclass.
	 */
	protected ByteArrayType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		throw new SQLException("byte[] type cannot have default values");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException {
		return results.getBlob(columnPos);
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		throw new SQLException("byte[] type cannot be converted from string to Java");
	}

	@Override
	public Class<?> getPrimaryClass() {
		return byte[].class;
	}
}
