package com.j256.ormlite.field.types;

import android.database.Cursor;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Type that persists a {@link BigInteger} object as a NUMERIC SQL database field.
 * 
 * @author graywatson
 */
public class BigDecimalNumericType extends BaseDataType {

	private static final BigDecimalNumericType singleTon = new BigDecimalNumericType();

	public static BigDecimalNumericType getSingleton() {
		return singleTon;
	}

	private BigDecimalNumericType() {
		// this has no classes because {@link BigDecimalString} is the default
		super(SqlType.BIG_DECIMAL);
	}

	/**
	 * Here for others to subclass.
	 */
	protected BigDecimalNumericType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		try {
			return new BigDecimal(defaultStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default BigDecimal string '"
					+ defaultStr + "'", e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException {
		throw new SQLException("Android does not support BigDecimal type.  Use BIG_DECIMAL or BIG_DECIMAL_STRING types");
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public Class<?> getPrimaryClass() {
		return BigDecimal.class;
	}
}
