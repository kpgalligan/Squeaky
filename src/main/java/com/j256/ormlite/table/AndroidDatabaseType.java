package com.j256.ormlite.table;

import android.database.Cursor;
import com.j256.ormlite.field.*;
import com.j256.ormlite.field.types.BigDecimalStringType;
import com.j256.ormlite.field.types.DateStringType;
import com.j256.ormlite.field.types.TimeStampStringType;
import com.j256.ormlite.field.types.TimeStampType;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by kgalligan on 6/15/15.
 */
public class AndroidDatabaseType
{
	public DataPersister getDataPersister(DataPersister defaultPersister) {
		if (defaultPersister == null) {
			return null;
		}
		// we are only overriding certain types
		switch (defaultPersister.getSqlType()) {
			case DATE :
				if (defaultPersister instanceof TimeStampType) {
					return TimeStampStringType.getSingleton();
				} else {
					return DateStringType.getSingleton();
				}
			default :
				return defaultPersister;
		}
	}

	private final static FieldConverter booleanConverter = new BooleanNumberFieldConverter();

	public FieldConverter getFieldConverter(DataPersister dataPersister) {
		// we are only overriding certain types
		switch (dataPersister.getSqlType()) {
			case BOOLEAN :
				return booleanConverter;
			case BIG_DECIMAL :
				return BigDecimalStringType.getSingleton();
			default :
				return dataPersister;
		}
	}

	protected static class BooleanNumberFieldConverter extends BaseFieldConverter
	{
		public SqlType getSqlType() {
			return SqlType.BOOLEAN;
		}
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			boolean bool = Boolean.parseBoolean(defaultStr);
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		@Override
		public Object javaToSqlArg(FieldType fieldType, Object obj) {
			Boolean bool = (Boolean) obj;
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException {
			return (byte)results.getShort(columnPos);
		}
		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			byte arg = (Byte) sqlArg;
			return arg == 1;
		}
		public Object resultToJava(FieldType fieldType, Cursor results, int columnPos) throws SQLException
		{
			return sqlArgToJava(fieldType, resultToSqlArg(fieldType, results, columnPos), columnPos);
		}

		public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) {
			return sqlArgToJava(fieldType, Byte.parseByte(stringValue), columnPos);
		}
	}

	protected void appendLongType(StringBuilder sb, FieldType fieldType) {
		/*
		 * This is unfortunate. SQLIte requires that a generated-id have the string "INTEGER PRIMARY KEY AUTOINCREMENT"
		 * even though the maximum generated value is 64-bit. See configureGeneratedId below.
		 */
		if (fieldType.getSqlType() == SqlType.LONG && fieldType.isGeneratedId()) {
			sb.append("INTEGER");
		} else {
			sb.append("BIGINT");
		}
	}

	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType) {
		/*
		 * Even though the documentation talks about INTEGER, it is 64-bit with a maximum value of 9223372036854775807.
		 * See http://www.sqlite.org/faq.html#q1 and http://www.sqlite.org/autoinc.html
		 */
		if (fieldType.getSqlType() != SqlType.INTEGER && fieldType.getSqlType() != SqlType.LONG) {
			throw new IllegalArgumentException(
					"Sqlite requires that auto-increment generated-id be integer or long type");
		}
		sb.append("PRIMARY KEY AUTOINCREMENT ");
		// no additional call to configureId here
	}

	public void appendColumnArg(StringBuilder sb, FieldType fieldType, List<String> additionalArgs) throws SQLException {
		appendEscapedEntityName(sb, fieldType.getColumnName());
		sb.append(' ');
		DataPersister dataPersister = fieldType.getDataPersister();
		// first try the per-field width

		switch (dataPersister.getSqlType()) {

			case STRING :
				appendStringType(sb);
				break;

			case LONG_STRING :
				appendLongStringType(sb);
				break;

			case BOOLEAN :
				appendBooleanType(sb);
				break;

			case DATE :
				appendDateType(sb);
				break;

			case CHAR :
				appendCharType(sb);
				break;

			case BYTE :
				appendByteType(sb);
				break;

			case BYTE_ARRAY :
				appendByteArrayType(sb);
				break;

			case SHORT :
				appendShortType(sb);
				break;

			case INTEGER :
				appendIntegerType(sb);
				break;

			case LONG :
				appendLongType(sb, fieldType);
				break;

			case FLOAT :
				appendFloatType(sb);
				break;

			case DOUBLE :
				appendDoubleType(sb);
				break;

			case SERIALIZABLE :
				appendSerializableType(sb);
				break;

			case BIG_DECIMAL :
				appendBigDecimalNumericType(sb);
				break;

			case UNKNOWN :
			default :
				// shouldn't be able to get here unless we have a missing case
				throw new IllegalArgumentException("Unknown SQL-type " + dataPersister.getSqlType());
		}
		sb.append(' ');

		/*
		 * NOTE: the configure id methods must be in this order since isGeneratedIdSequence is also isGeneratedId and
		 * isId. isGeneratedId is also isId.
		 */
		if (fieldType.isGeneratedId()) {
			configureGeneratedId(sb, fieldType);
		}
		// if we have a generated-id then neither the not-null nor the default make sense and cause syntax errors
		if (!fieldType.isGeneratedId()) {
			Object defaultValue = fieldType.getDefaultValue();
			if (defaultValue != null) {
				sb.append("DEFAULT ");
				appendDefaultValue(sb, fieldType, defaultValue);
				sb.append(' ');
			}
			if (!fieldType.isCanBeNull()) {
				sb.append("NOT NULL ");
			}
			if (fieldType.isUnique()) {
				addSingleUnique(fieldType, additionalArgs);
			}
		}
	}

	/**
	 * Output the SQL type for a Java String.
	 */
	protected void appendStringType(StringBuilder sb) {
		sb.append("VARCHAR");
	}

	/**
	 * Output the SQL type for a Java Long String.
	 */
	protected void appendLongStringType(StringBuilder sb) {
		sb.append("TEXT");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected void appendDateType(StringBuilder sb) {
		sb.append("TIMESTAMP");
	}

	/**
	 * Output the SQL type for a Java boolean.
	 */
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("BOOLEAN");
	}

	/**
	 * Output the SQL type for a Java char.
	 */
	protected void appendCharType(StringBuilder sb) {
		sb.append("CHAR");
	}

	/**
	 * Output the SQL type for a Java byte.
	 */
	protected void appendByteType(StringBuilder sb) {
		sb.append("TINYINT");
	}

	/**
	 * Output the SQL type for a Java short.
	 */
	protected void appendShortType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	/**
	 * Output the SQL type for a Java integer.
	 */
	private void appendIntegerType(StringBuilder sb) {
		sb.append("INTEGER");
	}

	/**
	 * Output the SQL type for a Java float.
	 */
	private void appendFloatType(StringBuilder sb) {
		sb.append("FLOAT");
	}

	/**
	 * Output the SQL type for a Java double.
	 */
	private void appendDoubleType(StringBuilder sb) {
		sb.append("DOUBLE PRECISION");
	}

	/**
	 * Output the SQL type for either a serialized Java object or a byte[].
	 */
	protected void appendByteArrayType(StringBuilder sb) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a serialized Java object.
	 */
	protected void appendSerializableType(StringBuilder sb) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a BigDecimal object.
	 */
	protected void appendBigDecimalNumericType(StringBuilder sb) {
		sb.append("NUMERIC");
	}

	/**
	 * Output the SQL type for the default value for the type.
	 */
	private void appendDefaultValue(StringBuilder sb, FieldType fieldType, Object defaultValue) {
		if (fieldType.isEscapedDefaultValue()) {
			appendEscapedWord(sb, defaultValue.toString());
		} else {
			sb.append(defaultValue);
		}
	}

	public void addPrimaryKeySql(FieldType[] fieldTypes, List<String> additionalArgs) {
		StringBuilder sb = null;
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.isGeneratedId()) {
				// don't add anything
			} else if (fieldType.isId()) {
				if (sb == null) {
					sb = new StringBuilder(48);
					sb.append("PRIMARY KEY (");
				} else {
					sb.append(',');
				}
				appendEscapedEntityName(sb, fieldType.getColumnName());
			}
		}
		if (sb != null) {
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	public void addUniqueComboSql(FieldType[] fieldTypes, List<String> additionalArgs) {
		StringBuilder sb = null;
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.isUniqueCombo()) {
				if (sb == null) {
					sb = new StringBuilder(48);
					sb.append("UNIQUE (");
				} else {
					sb.append(',');
				}
				appendEscapedEntityName(sb, fieldType.getColumnName());
			}
		}
		if (sb != null) {
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	public void appendEscapedWord(StringBuilder sb, String word) {
		sb.append('\'').append(word).append('\'');
	}

	public void appendEscapedEntityName(StringBuilder sb, String name) {
		sb.append('`').append(name).append('`');
	}

	/**
	 * Add SQL to handle a unique=true field. THis is not for uniqueCombo=true.
	 */
	private void addSingleUnique(FieldType fieldType, List<String> additionalArgs) {
		StringBuilder alterSb = new StringBuilder();
		alterSb.append(" UNIQUE (");
		appendEscapedEntityName(alterSb, fieldType.getColumnName());
		alterSb.append(")");
		additionalArgs.add(alterSb.toString());
	}
}
