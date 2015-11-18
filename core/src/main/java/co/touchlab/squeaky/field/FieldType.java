package co.touchlab.squeaky.field;

import android.database.Cursor;
import android.text.TextUtils;
import co.touchlab.squeaky.field.types.BigDecimalStringType;
import co.touchlab.squeaky.field.types.DateStringType;
import co.touchlab.squeaky.field.types.TimeStampStringType;
import co.touchlab.squeaky.field.types.TimeStampType;

import java.sql.SQLException;

/**
 * @author graywatson
 */
public class FieldType
{

	/**
	 * default suffix added to fields that are id fields of foreign objects
	 */
	public static final String FOREIGN_ID_FIELD_SUFFIX = "_id";

	/*
	 * Default values.
	 * 
	 * NOTE: These don't get any values so the compiler assigns them to the default values for the type. Ahhhh. Smart.
	 */
	private static boolean DEFAULT_VALUE_BOOLEAN;
	private static byte DEFAULT_VALUE_BYTE;
	private static char DEFAULT_VALUE_CHAR;
	private static short DEFAULT_VALUE_SHORT;
	private static int DEFAULT_VALUE_INT;
	private static long DEFAULT_VALUE_LONG;
	private static float DEFAULT_VALUE_FLOAT;
	private static double DEFAULT_VALUE_DOUBLE;

	private final String columnName;
	private final boolean isId;
	private final boolean isGeneratedId;
	private final boolean isForeign;
	private final String fieldName;
	private final DataType dataType;
	private final Class fieldType;
	private final boolean canBeNull;
	private final String format;
	private final boolean unique;
	private final boolean uniqueCombo;
	private final boolean index;
	private final boolean uniqueIndex;
	private final boolean foreignAutoRefresh;
	private String indexNameBase;
	private String indexName;
	private String uniqueIndexName;

	private DataPersister dataPersister;
	private Object defaultValue;
	private Object dataTypeConfigObj;

	private FieldConverter fieldConverter;

	public FieldType(
			String indexNameBase,
			String fieldName,
			String columnName,
			boolean isId,
			boolean isGeneratedId,
			boolean isForeign,
			DataType dataType,
			Class fieldType,
			boolean canBeNull,
			String format,
			boolean unique,
			boolean uniqueCombo,
			boolean index,
			boolean uniqueIndex,
			String indexName,
			String uniqueIndexName,
			String configDefaultValue,
			boolean foreignAutoRefresh)
	{
		this.fieldName = fieldName;
		this.indexNameBase = indexNameBase;
		this.canBeNull = canBeNull;
		this.format = format;
		this.unique = unique;
		this.uniqueCombo = uniqueCombo;
		this.index = index;
		this.uniqueIndex = uniqueIndex;
		this.indexName = indexName;
		this.uniqueIndexName = uniqueIndexName;
		this.foreignAutoRefresh = foreignAutoRefresh;
		this.dataPersister = dataType.getDataPersister();
		this.isForeign = isForeign;
		this.dataType = dataType;
		this.fieldType = fieldType;
		this.columnName = columnName;
		this.isId = isId;
		this.isGeneratedId = isGeneratedId;

		//TODO: Move this to annotation processor code
		if ((this.isId || this.isGeneratedId) && this.isForeign)
		{
			throw new IllegalArgumentException("Id field " + fieldName + " cannot also be a foreign object");
		}

		try
		{
			assignDataType(dataPersister, configDefaultValue);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public DataPersister getDataPersister()
	{
		return dataPersister;
	}

	public Object getDataTypeConfigObj()
	{
		return dataTypeConfigObj;
	}

	public SqlType getSqlType()
	{
		return fieldConverter.getSqlType();
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public boolean isCanBeNull()
	{
		return canBeNull;
	}

	public Class getFieldType()
	{
		return fieldType;
	}

	public boolean isId()
	{
		return isId;
	}

	public boolean isGeneratedId()
	{
		return isGeneratedId;
	}

	public boolean isForeign()
	{
		return isForeign;
	}

	/**
	 * Convert a field value to something suitable to be stored in the database.
	 */
	public Object convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException
	{
		if (fieldVal == null)
		{
			return null;
		}
		else
		{
			return fieldConverter.javaToSqlArg(this, fieldVal);
		}
	}

	/**
	 * Call through to {@link DataPersister#isEscapedValue()}
	 */
	public boolean isEscapedValue()
	{
		return dataPersister.isEscapedValue();
	}

	public boolean isForeignAutoRefresh()
	{
		return foreignAutoRefresh;
	}

	/**
	 * Return the format of the field.
	 */
	public String getFormat()
	{
		return format;
	}

	public boolean isUnique()
	{
		return unique;
	}

	public boolean isUniqueCombo()
	{
		return uniqueCombo;
	}

	public String getIndexName()
	{
		return getIndexName(indexNameBase);
	}

	public String getIndexName(String indexNameBase)
	{
		if (index && TextUtils.isEmpty(indexName))
		{
			indexName = findIndexName(indexNameBase);
		}
		return indexName;
	}

	public String getUniqueIndexName(String indexNameBase)
	{
		if (uniqueIndex && (uniqueIndexName == null || uniqueIndexName.equals("")))
		{
			uniqueIndexName = findIndexName(indexNameBase);
		}
		return uniqueIndexName;
	}

	private String findIndexName(String indexNameBase)
	{
		if (columnName == null)
		{
			return indexNameBase + "_" + fieldName + "_idx";
		}
		else
		{
			return indexNameBase + "_" + columnName + "_idx";
		}
	}

	public String getUniqueIndexName()
	{
		return getUniqueIndexName(indexNameBase);
	}

	public DataType getDataType()
	{
		return dataType;
	}

	/**
	 * Call through to {@link DataPersister#isEscapedDefaultValue()}
	 */
	public boolean isEscapedDefaultValue()
	{
		return dataPersister.isEscapedDefaultValue();
	}

	/**
	 * Call through to {@link DataPersister#isComparable()}
	 */
	public boolean isComparable() throws SQLException
	{
		/*
		 * We've seen dataPersister being null here in some strange cases. Why? It may because someone is searching on
		 * an improper field. Or maybe a table-config does not match the Java object?
		 */
		if (dataPersister == null)
		{
			throw new SQLException("Internal error.  Data-persister is not configured for field.  "
					+ "Please post _full_ exception with associated data objects to mailing list: " + this);
		}
		else
		{
			return dataPersister.isComparable();
		}
	}

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	public Object getJavaDefaultValueDefault()
	{
		if (dataType == null)
			return null;
		else if (dataType == DataType.BOOLEAN)
		{
			return DEFAULT_VALUE_BOOLEAN;
		}
		else if (dataType == DataType.BYTE || dataType == DataType.CHAR_OBJ)
		{
			return DEFAULT_VALUE_BYTE;
		}
		else if (dataType == DataType.CHAR || dataType == DataType.CHAR_OBJ)
		{
			return DEFAULT_VALUE_CHAR;
		}
		else if (dataType == DataType.SHORT || dataType == DataType.SHORT_OBJ)
		{
			return DEFAULT_VALUE_SHORT;
		}
		else if (dataType == DataType.INTEGER || dataType == DataType.INTEGER_OBJ)
		{
			return DEFAULT_VALUE_INT;
		}
		else if (dataType == DataType.LONG || dataType == DataType.LONG_OBJ)
		{
			return DEFAULT_VALUE_LONG;
		}
		else if (dataType == DataType.FLOAT || dataType == DataType.FLOAT_OBJ)
		{
			return DEFAULT_VALUE_FLOAT;
		}
		else if (dataType == DataType.DOUBLE || dataType == DataType.DOUBLE_OBJ)
		{
			return DEFAULT_VALUE_DOUBLE;
		}
		else
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":name=" + fieldName + ",type="
				+ dataType;
	}

	private void assignDataType(DataPersister dataPersister, String defaultStr) throws SQLException
	{

		dataPersister = getDataPersister(dataPersister);
		this.dataPersister = dataPersister;
		this.fieldConverter = getFieldConverter(dataPersister);

		this.dataTypeConfigObj = dataPersister.makeConfigObject(this);
		if (defaultStr == null)
		{
			this.defaultValue = null;
		}
		else if (this.isGeneratedId)
		{
			throw new SQLException("Field '" + fieldName + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		}
		else
		{
			this.defaultValue = this.fieldConverter.parseDefaultString(this, defaultStr);
		}
	}

	public DataPersister getDataPersister(DataPersister defaultPersister)
	{
		if (defaultPersister == null)
		{
			return null;
		}
		// we are only overriding certain types
		switch (defaultPersister.getSqlType())
		{
			case DATE:
				if (defaultPersister instanceof TimeStampType)
				{
					return TimeStampStringType.getSingleton();
				}
				else
				{
					return DateStringType.getSingleton();
				}
			default:
				return defaultPersister;
		}
	}

	private final static FieldConverter booleanConverter = new BooleanNumberFieldConverter();

	public FieldConverter getFieldConverter(DataPersister dataPersister)
	{
		// we are only overriding certain types
		switch (dataPersister.getSqlType())
		{
			case BOOLEAN:
				return booleanConverter;
			case BIG_DECIMAL:
				return BigDecimalStringType.getSingleton();
			default:
				return dataPersister;
		}
	}

	protected static class BooleanNumberFieldConverter extends BaseFieldConverter
	{
		public SqlType getSqlType()
		{
			return SqlType.BOOLEAN;
		}

		public Object parseDefaultString(FieldType fieldType, String defaultStr)
		{
			boolean bool = Boolean.parseBoolean(defaultStr);
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object obj)
		{
			Boolean bool = (Boolean) obj;
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}

		public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
		{
			return (byte) results.getShort(columnPos);
		}

		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
		{
			byte arg = (Byte) sqlArg;
			return arg == 1;
		}

		public Object resultToJava(FieldType fieldType, Cursor results, int columnPos) throws SQLException
		{
			return sqlArgToJava(fieldType, resultToSqlArg(fieldType, results, columnPos), columnPos);
		}
	}
}
