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
	public DataPersister getDataPersister(DataPersister defaultPersister, FieldType fieldType) {
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

	public FieldConverter getFieldConverter(DataPersister dataPersister, FieldType fieldType) {
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
			boolean bool = (boolean) Boolean.parseBoolean(defaultStr);
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
			return (arg == 1 ? (Boolean) true : (Boolean) false);
		}
		public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) {
			return sqlArgToJava(fieldType, Byte.parseByte(stringValue), columnPos);
		}
	}

	protected void appendLongType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
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

	protected void configureGeneratedId(String tableName, StringBuilder sb, FieldType fieldType,
										List<String> statementsBefore, List<String> statementsAfter, List<String> additionalArgs,
										List<String> queriesAfter) {
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

	public void appendColumnArg(String tableName, StringBuilder sb, FieldType fieldType, List<String> additionalArgs,
								List<String> statementsBefore, List<String> statementsAfter, List<String> queriesAfter) throws SQLException {
		appendEscapedEntityName(sb, fieldType.getColumnName());
		sb.append(' ');
		DataPersister dataPersister = fieldType.getDataPersister();
		// first try the per-field width
		int fieldWidth = fieldType.getWidth();
		if (fieldWidth == 0) {
			// next try the per-data-type width
			fieldWidth = dataPersister.getDefaultWidth();
		}
		switch (dataPersister.getSqlType()) {

			case STRING :
				appendStringType(sb, fieldType, fieldWidth);
				break;

			case LONG_STRING :
				appendLongStringType(sb, fieldType, fieldWidth);
				break;

			case BOOLEAN :
				appendBooleanType(sb, fieldType, fieldWidth);
				break;

			case DATE :
				appendDateType(sb, fieldType, fieldWidth);
				break;

			case CHAR :
				appendCharType(sb, fieldType, fieldWidth);
				break;

			case BYTE :
				appendByteType(sb, fieldType, fieldWidth);
				break;

			case BYTE_ARRAY :
				appendByteArrayType(sb, fieldType, fieldWidth);
				break;

			case SHORT :
				appendShortType(sb, fieldType, fieldWidth);
				break;

			case INTEGER :
				appendIntegerType(sb, fieldType, fieldWidth);
				break;

			case LONG :
				appendLongType(sb, fieldType, fieldWidth);
				break;

			case FLOAT :
				appendFloatType(sb, fieldType, fieldWidth);
				break;

			case DOUBLE :
				appendDoubleType(sb, fieldType, fieldWidth);
				break;

			case SERIALIZABLE :
				appendSerializableType(sb, fieldType, fieldWidth);
				break;

			case BIG_DECIMAL :
				appendBigDecimalNumericType(sb, fieldType, fieldWidth);
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
			configureGeneratedId(tableName, sb, fieldType, statementsBefore, statementsAfter, additionalArgs,
					queriesAfter);
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
				addSingleUnique(sb, fieldType, additionalArgs, statementsAfter);
			}
		}
	}

	/**
	 * Output the SQL type for a Java String.
	 */
	protected void appendStringType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("VARCHAR");
	}

	/**
	 * Output the SQL type for a Java Long String.
	 */
	protected void appendLongStringType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("TEXT");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected void appendDateType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("TIMESTAMP");
	}

	/**
	 * Output the SQL type for a Java boolean.
	 */
	protected void appendBooleanType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("BOOLEAN");
	}

	/**
	 * Output the SQL type for a Java char.
	 */
	protected void appendCharType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("CHAR");
	}

	/**
	 * Output the SQL type for a Java byte.
	 */
	protected void appendByteType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("TINYINT");
	}

	/**
	 * Output the SQL type for a Java short.
	 */
	protected void appendShortType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("SMALLINT");
	}

	/**
	 * Output the SQL type for a Java integer.
	 */
	private void appendIntegerType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("INTEGER");
	}

	/**
	 * Output the SQL type for a Java float.
	 */
	private void appendFloatType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("FLOAT");
	}

	/**
	 * Output the SQL type for a Java double.
	 */
	private void appendDoubleType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("DOUBLE PRECISION");
	}

	/**
	 * Output the SQL type for either a serialized Java object or a byte[].
	 */
	protected void appendByteArrayType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a serialized Java object.
	 */
	protected void appendSerializableType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a BigDecimal object.
	 */
	protected void appendBigDecimalNumericType(StringBuilder sb, FieldType fieldType, int fieldWidth) {
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

	public void addPrimaryKeySql(FieldType[] fieldTypes, List<String> additionalArgs, List<String> statementsBefore,
								 List<String> statementsAfter, List<String> queriesAfter) {
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

	public void addUniqueComboSql(FieldType[] fieldTypes, List<String> additionalArgs, List<String> statementsBefore,
								  List<String> statementsAfter, List<String> queriesAfter) {
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
	private void addSingleUnique(StringBuilder sb, FieldType fieldType, List<String> additionalArgs,
								 List<String> statementsAfter) {
		StringBuilder alterSb = new StringBuilder();
		alterSb.append(" UNIQUE (");
		appendEscapedEntityName(alterSb, fieldType.getColumnName());
		alterSb.append(")");
		additionalArgs.add(alterSb.toString());
	}
}
