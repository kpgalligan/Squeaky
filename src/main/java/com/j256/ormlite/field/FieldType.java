package com.j256.ormlite.field;

import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * @author graywatson
 */
public class FieldType<T, ID> {

	/** default suffix added to fields that are id fields of foreign objects */
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

	private final String tableName;
	private final String columnName;
	private final boolean isId;
	private final boolean isGeneratedId;
	private final boolean isForeign;
	private final String fieldName;
	private final DataType dataType;
	private final int width;
	private final boolean canBeNull;
	private final String format;
	private final boolean unique;
	private final boolean uniqueCombo;
	private final boolean index;
	private final boolean uniqueIndex;
	private final boolean throwIfNull;
	private final boolean version;
	private final boolean readOnly;
	private String indexName;
	private String uniqueIndexName;

	private DataPersister dataPersister;
	private Object defaultValue;
	private Object dataTypeConfigObj;

	private FieldConverter fieldConverter;

	/**
	 * ThreadLocal counters to detect initialization loops. Notice that there is _not_ an initValue() method on purpose.
	 * We don't want to create these if we don't have to.
	 */
	private static final ThreadLocal<LevelCounters> threadLevelCounters = new ThreadLocal<LevelCounters>();

	public FieldType(
			String tableName,
			String fieldName,
			String columnName,
			boolean isId,
			boolean isGeneratedId,
			boolean isForeign,
			DataType dataType,
			int width,
			boolean canBeNull,
			String format,
			boolean unique,
			boolean uniqueCombo,
			boolean index,
			boolean uniqueIndex,
			String indexName,
			String uniqueIndexName,
			String configDefaultValue,
			boolean throwIfNull,
			boolean version,
			boolean readOnly){
		this.fieldName = fieldName;
		this.tableName = tableName;
		this.width = width;
		this.canBeNull = canBeNull;
		this.format = format;
		this.unique = unique;
		this.uniqueCombo = uniqueCombo;
		this.index = index;
		this.uniqueIndex = uniqueIndex;
		this.indexName = indexName;
		this.uniqueIndexName = uniqueIndexName;
		this.throwIfNull = throwIfNull;
		this.version = version;
		this.readOnly = readOnly;
		this.dataPersister = dataType.getDataPersister();
		this.isForeign = isForeign;
		this.dataType = dataType;
		this.columnName = columnName;
		this.isId = isId;
		this.isGeneratedId = isGeneratedId;

		//TODO: Move this to annotation processor code
		if ((this.isId || this.isGeneratedId )&& this.isForeign) {
			throw new IllegalArgumentException("Id field " + fieldName + " cannot also be a foreign object");
		}

		try
		{
			assignDataType(dataPersister, configDefaultValue);
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getColumnName() {
		return columnName;
	}

	public DataPersister getDataPersister() {
		return dataPersister;
	}

	public Object getDataTypeConfigObj() {
		return dataTypeConfigObj;
	}

	public SqlType getSqlType() {
		return fieldConverter.getSqlType();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public int getWidth() {
		return width;
	}

	public boolean isCanBeNull() {
		return canBeNull;
	}

	/**
	 * Return whether the field is an id field. It is an id if {@link DatabaseField#id},
	 * {@link DatabaseField#generatedId}, OR {@link DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isId() {
		return isId;
	}

	/**
	 * Return whether the field is a generated-id field. This is true if {@link DatabaseField#generatedId} OR
	 * {@link DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isGeneratedId() {
		return isGeneratedId;
	}

	public boolean isForeign() {
		return isForeign;
	}

	/**
	 * Convert a field value to something suitable to be stored in the database.
	 */
	public Object convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException {
		if (fieldVal == null) {
			return null;
		} else {
			return fieldConverter.javaToSqlArg(this, fieldVal);
		}
	}

	/**
	 * Call through to {@link DataPersister#isEscapedValue()}
	 */
	public boolean isEscapedValue() {
		return dataPersister.isEscapedValue();
	}

	//Figure out enums
	public Enum<?> getUnknownEnumVal() {
		return null;
	}

	/**
	 * Return the format of the field.
	 */
	public String getFormat() {
		return format;
	}

	public boolean isUnique() {
		return unique;
	}

	public boolean isUniqueCombo() {
		return uniqueCombo;
	}

	public String getIndexName() {
		return getIndexName(tableName);
	}

	public String getIndexName(String tableName) {
		if (index && (indexName == null || indexName.equals(""))) {
			indexName = findIndexName(tableName);
		}
		return indexName;
	}

	public String getUniqueIndexName(String tableName) {
		if (uniqueIndex && (uniqueIndexName == null || uniqueIndexName.equals(""))) {
			uniqueIndexName = findIndexName(tableName);
		}
		return uniqueIndexName;
	}

	private String findIndexName(String tableName) {
		if (columnName == null) {
			return tableName + "_" + fieldName + "_idx";
		} else {
			return tableName + "_" + columnName + "_idx";
		}
	}

	public String getUniqueIndexName() {
		return getUniqueIndexName(tableName);
	}

	public DataType getDataType()
	{
		return dataType;
	}

	/**
	 * Call through to {@link DataPersister#isEscapedDefaultValue()}
	 */
	public boolean isEscapedDefaultValue() {
		return dataPersister.isEscapedDefaultValue();
	}

	/**
	 * Call through to {@link DataPersister#isComparable()}
	 */
	public boolean isComparable() throws SQLException {
		/*
		 * We've seen dataPersister being null here in some strange cases. Why? It may because someone is searching on
		 * an improper field. Or maybe a table-config does not match the Java object?
		 */
		if (dataPersister == null) {
			throw new SQLException("Internal error.  Data-persister is not configured for field.  "
					+ "Please post _full_ exception with associated data objects to mailing list: " + this);
		} else {
			return dataPersister.isComparable();
		}
	}

	/**
	 * Call through to {@link DataPersister#isArgumentHolderRequired()}
	 */
	public boolean isArgumentHolderRequired() {
		return dataPersister.isArgumentHolderRequired();
	}

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	public Object getJavaDefaultValueDefault() {
		if(dataType == null)
			return null;
		else if (dataType == DataType.BOOLEAN) {
			return DEFAULT_VALUE_BOOLEAN;
		} else if (dataType == DataType.BYTE || dataType == DataType.CHAR_OBJ) {
			return DEFAULT_VALUE_BYTE;
		} else if (dataType == DataType.CHAR || dataType == DataType.CHAR_OBJ) {
			return DEFAULT_VALUE_CHAR;
		} else if (dataType == DataType.SHORT || dataType == DataType.SHORT_OBJ) {
			return DEFAULT_VALUE_SHORT;
		} else if (dataType == DataType.INTEGER || dataType == DataType.INTEGER_OBJ) {
			return DEFAULT_VALUE_INT;
		} else if (dataType == DataType.LONG || dataType == DataType.LONG_OBJ) {
			return DEFAULT_VALUE_LONG;
		} else if (dataType == DataType.FLOAT || dataType == DataType.FLOAT_OBJ) {
			return DEFAULT_VALUE_FLOAT;
		} else if (dataType == DataType.DOUBLE || dataType == DataType.DOUBLE_OBJ) {
			return DEFAULT_VALUE_DOUBLE;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":name=" + fieldName + ",type="
				+ dataType;
	}

	private void assignDataType(DataPersister dataPersister, String defaultStr) throws SQLException {

		dataPersister = TableUtils.databaseType.getDataPersister(dataPersister, this);
		this.dataPersister = dataPersister;
		this.fieldConverter = TableUtils.databaseType.getFieldConverter(dataPersister, this);

		//TODO: Probably move to annotation processor
		if (this.isGeneratedId && !dataPersister.isValidGeneratedType()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Generated-id field '").append(fieldName);
			sb.append("'");
			sb.append(" can't be type ").append(dataPersister.getSqlType());
			sb.append(".  Must be one of: ");
			for (DataType dataType : DataType.values()) {
				DataPersister persister = dataType.getDataPersister();
				if (persister != null && persister.isValidGeneratedType()) {
					sb.append(dataType).append(' ');
				}
			}
			throw new IllegalArgumentException(sb.toString());
		}

		this.dataTypeConfigObj = dataPersister.makeConfigObject(this);
		if (defaultStr == null) {
			this.defaultValue = null;
		} else if (this.isGeneratedId) {
			throw new SQLException("Field '" + fieldName + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		} else {
			this.defaultValue = this.fieldConverter.parseDefaultString(this, defaultStr);
		}
	}

	private static class LevelCounters {
		// current auto-refresh recursion level
		int autoRefreshLevel;
		// maximum auto-refresh recursion level
		int autoRefreshLevelMax;

		// current foreign-collection recursion level
		int foreignCollectionLevel;
		// maximum foreign-collection recursion level
		int foreignCollectionLevelMax;
	}
}
