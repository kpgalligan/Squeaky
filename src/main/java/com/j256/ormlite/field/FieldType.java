package com.j256.ormlite.field;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

import java.sql.SQLException;
import java.util.Map;

/**
 * @author graywatson
 */
public class FieldType<T, ID> {

	/*public interface Implem
	{
		void assignField(Object data, Object val, boolean parentObject) throws SQLException;
		<FV> FV extractRawJavaFieldValue(Object object) throws SQLException ;
	}*/

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
	private boolean foreign;
	private FieldType foreignIdField;
	private TableInfo<?, ?> foreignTableInfo;
	private FieldType foreignFieldType;
	private BaseDaoImpl<?, ?> foreignDao;
	private MappedQueryForId<Object, Object> mappedQueryForId;

	/**
	 * ThreadLocal counters to detect initialization loops. Notice that there is _not_ an initValue() method on purpose.
	 * We don't want to create these if we don't have to.
	 */
	private static final ThreadLocal<LevelCounters> threadLevelCounters = new ThreadLocal<LevelCounters>();

	public FieldType(
			DatabaseType databaseType,
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
			boolean readOnly) throws SQLException {
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

		// post process our config settings
//		fieldConfig.postProcess();


//		String foreignColumnName = fieldConfig.getForeignColumnName();
		/*if (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh() || foreignColumnName != null) {
			if (dataPersister != null && dataPersister.isPrimitive()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class "
						+ " but marked as foreign");
			}
			if (foreignColumnName == null) {
				defaultFieldName = defaultFieldName + FOREIGN_ID_FIELD_SUFFIX;
			} else {
				defaultFieldName = defaultFieldName + "_" + foreignColumnName;
			}
		}  else if (dataPersister == null) {
			if (byte[].class.isAssignableFrom(clazz)) {
				throw new SQLException("ORMLite does not know how to store " + clazz + " for field '" + fieldName
						+ "'. byte[] fields must specify dataType=DataType.BYTE_ARRAY or SERIALIZABLE");
			} else if (Serializable.class.isAssignableFrom(clazz)) {
				throw new SQLException("ORMLite does not know how to store " + clazz + " for field '" + fieldName
						+ "'.  Use another class, custom persister, or to serialize it use "
						+ "dataType=DataType.SERIALIZABLE");
			} else {
				throw new IllegalArgumentException("ORMLite does not know how to store " + clazz + " for field "
						+ fieldName + ". Use another class or a custom persister.");
			}
		}*/

		this.columnName = columnName;
		this.isId = isId;
		this.isGeneratedId = isGeneratedId;

		//TODO: Move this to annotation processor code
		if ((this.isId || this.isGeneratedId )&& this.isForeign) {
			throw new IllegalArgumentException("Id field " + fieldName + " cannot also be a foreign object");
		}
		//extra validation
		/*if (fieldConfig.isAllowGeneratedIdInsert() && !fieldConfig.isGeneratedId()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must be a generated-id if allowGeneratedIdInsert = true");
		}
		if (fieldConfig.isForeignAutoRefresh() && !fieldConfig.isForeign()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must have foreign = true if foreignAutoRefresh = true");
		}
		if (fieldConfig.isForeignAutoCreate() && !fieldConfig.isForeign()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must have foreign = true if foreignAutoCreate = true");
		}
		if (fieldConfig.getForeignColumnName() != null && !fieldConfig.isForeign()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must have foreign = true if foreignColumnName is set");
		}
		if (fieldConfig.isVersion() && (dataPersister == null || !dataPersister.isValidForVersion())) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " is not a valid type to be a version field");
		}*/
		assignDataType(databaseType, dataPersister, configDefaultValue);
	}

	/**
	 * Because we go recursive in a lot of situations if we construct DAOs inside of the FieldType constructor, we have
	 * to do this 2nd pass initialization so we can better use the DAO caches.
	 * 
	 * @see BaseDaoImpl#initialize()
	 */
	/*public void configDaoInformation(ConnectionSource connectionSource, Class<?> parentClass) throws SQLException {
		Class<?> fieldClass = field.getType();
		DatabaseType databaseType = connectionSource.getDatabaseType();
		TableInfo<?, ?> foreignTableInfo;
		final FieldType foreignIdField;
		final FieldType foreignFieldType;
		final BaseDaoImpl<?, ?> foreignDao;
		final MappedQueryForId<Object, Object> mappedQueryForId;

		String foreignColumnName = fieldConfig.getForeignColumnName();
		if (fieldConfig.isForeignAutoRefresh() || foreignColumnName != null) {
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig == null) {
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, fieldClass);
				foreignTableInfo = foreignDao.getTableInfo();
			} else {
				tableConfig.extractFieldTypes(connectionSource);
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, tableConfig);
				foreignTableInfo = foreignDao.getTableInfo();
			}
			if (foreignColumnName == null) {
				foreignIdField = foreignTableInfo.getIdField();
				if (foreignIdField == null) {
					throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have id field");
				}
			} else {
				foreignIdField = foreignTableInfo.getFieldTypeByColumnName(foreignColumnName);
				if (foreignIdField == null) {
					throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have field named '"
							+ foreignColumnName + "'");
				}
			}
			@SuppressWarnings("unchecked")
			MappedQueryForId<Object, Object> castMappedQueryForId =
					(MappedQueryForId<Object, Object>) MappedQueryForId.build(databaseType, foreignTableInfo,
							foreignIdField);
			mappedQueryForId = castMappedQueryForId;
			foreignFieldType = null;
		} else if (fieldConfig.isForeign()) {
			if (this.dataPersister != null && this.dataPersister.isPrimitive()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class " + fieldClass
						+ " but marked as foreign");
			}
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig != null) {
				tableConfig.extractFieldTypes(connectionSource);
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, tableConfig);
			} else {
				*//*
				 * Initially we were only doing this just for BaseDaoEnabled.class and isForeignAutoCreate(). But we
				 * need it also for foreign fields because the alternative was to use reflection. Chances are if it is
				 * foreign we're going to need the DAO in the future anyway so we might as well create it. This also
				 * allows us to make use of any table configs.
				 *//*
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, fieldClass);
			}
			foreignTableInfo = foreignDao.getTableInfo();
			foreignIdField = foreignTableInfo.getIdField();
			if (foreignIdField == null) {
				throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have id field");
			}
			if (isForeignAutoCreate() && !foreignIdField.isGeneratedId()) {
				throw new IllegalArgumentException("Field " + field.getName()
						+ ", if foreignAutoCreate = true then class " + fieldClass.getSimpleName()
						+ " must have id field with generatedId = true");
			}
			foreignFieldType = null;
			mappedQueryForId = null;
		} else {
			foreignTableInfo = null;
			foreignIdField = null;
			foreignFieldType = null;
			foreignDao = null;
			mappedQueryForId = null;
		}

		this.mappedQueryForId = mappedQueryForId;
		this.foreignTableInfo = foreignTableInfo;
		this.foreignFieldType = foreignFieldType;
		this.foreignDao = foreignDao;
		this.foreignIdField = foreignIdField;

		// we have to do this because if we habe a foreign field then our id type might have gone to an _id primitive
		if (this.foreignIdField != null) {
			assignDataType(databaseType, this.foreignIdField.getDataPersister());
		}
	}*/

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

	/*public void assignField(Object data, Object val, boolean parentObject) throws SQLException
	{
		implem.assignField(data, val, parentObject);
	}

	public <FV> FV extractRawJavaFieldValue(Object object) throws SQLException
	{
		return implem.extractRawJavaFieldValue(object);
	}*/

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	/*public void assignField(Object data, Object val, boolean parentObject) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
		*//*if (foreignIdField != null && val != null) {
			// get the current field value which is the foreign-id
			Object foreignId = extractJavaFieldValue(data);
			*//**//*
			 * See if we don't need to create a new foreign object. If we are refreshing and the id field has not
			 * changed then there is no need to create a new foreign object and maybe lose previously refreshed field
			 * information.
			 *//**//*
			if (foreignId != null && foreignId.equals(val)) {
				return;
			}
			// awhitlock: raised as OrmLite issue: bug #122
			Object cachedVal;
			ObjectCache foreignCache = foreignDao.getObjectCache();
			if (foreignCache == null) {
				cachedVal = null;
			} else {
				cachedVal = foreignCache.get(getType(), val);
			}
			if (cachedVal != null) {
				val = cachedVal;
			} else if (!parentObject) {
				// the value we are to assign to our field is now the foreign object itself
				val = createForeignObject(val, objectCache);
			}
		}*//*

		if (fieldSetMethod == null) {
			try {
				field.set(data, val);
			} catch (IllegalArgumentException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' of type " + val.getClass()
						+ " to field " + this, e);
			} catch (IllegalAccessException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' of type " + val.getClass()
						+ "' to field " + this, e);
			}
		} else {
			try {
				fieldSetMethod.invoke(data, val);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call " + fieldSetMethod + " on object with '" + val + "' for "
						+ this, e);
			}
		}
	}*/

	/*{
		Object val;
			try {
				// field object may not be a T yet
				val = field.get(object);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not get field value for " + this, e);
			}

		@SuppressWarnings("unchecked")
		FV converted = (FV) val;
		return converted;
	}*/

	/*private Object extractJavaFieldValue(Object object) throws SQLException {

		Object val = implem.extractRawJavaFieldValue(object);

		// if this is a foreign object then we want its id field
		if (foreignIdField != null && val != null) {
			val = foreignIdField.implem.extractRawJavaFieldValue(val);
		}

		return val;
	}*/

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
	 * Convert a string value into the appropriate Java field value.
	 */
	public Object convertStringToJavaField(String value, int columnPos) throws SQLException {
		if (value == null) {
			return null;
		} else {
			return fieldConverter.resultStringToJava(this, value, columnPos);
		}
	}

	/**
	 * Move the SQL value to the next one for version processing.
	 */
	public Object moveToNextValue(Object val) throws SQLException {
		if (dataPersister == null) {
			return null;
		} else {
			return dataPersister.moveToNextValue(val);
		}
	}

	/**
	 * Return the id field associated with the foreign object or null if none.
	 */
	public FieldType getForeignIdField() {
		return foreignIdField;
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
	 * Get the result object from the results. A call through to {@link FieldConverter#resultToJava}.
	 */
	public <T> T resultToJava(DatabaseResults results, Map<String, Integer> columnPositions) throws SQLException {
		Integer dbColumnPos = columnPositions.get(columnName);
		if (dbColumnPos == null) {
			dbColumnPos = results.findColumn(columnName);
			columnPositions.put(columnName, dbColumnPos);
		}
		@SuppressWarnings("unchecked")
		T converted = (T) fieldConverter.resultToJava(this, results, dbColumnPos);
		if (isForeign()) {
			/*
			 * Subtle problem here. If your foreign field is a primitive and the value was null then this would return 0
			 * from getInt(). We have to specifically test to see if we have a foreign field so if it is null we return
			 * a null value to not create the sub-object.
			 */
			if (results.wasNull(dbColumnPos)) {
				return null;
			}
		} else if (dataPersister.isPrimitive()) {
			if (throwIfNull && results.wasNull(dbColumnPos)) {
				throw new SQLException("Results value for primitive field '" + fieldName
						+ "' was an invalid null value");
			}
		} else if (!fieldConverter.isStreamType() && results.wasNull(dbColumnPos)) {
			// we can't check if we have a null if this is a stream type
			return null;
		}
		return converted;
	}

	/**
	 * Call through to {@link DataPersister#isSelfGeneratedId()}
	 */
	public boolean isSelfGeneratedId() {
		return dataPersister.isSelfGeneratedId();
	}

	/**
	 * TODO: What the fuck is this?
	 */
	public boolean isAllowGeneratedIdInsert() {
		return false;
	}

/*
	*/
/*

	public boolean isForeignAutoCreate() {
		return fieldConfig.isForeignAutoCreate();
	}
*/

	public boolean isVersion() {
		return version;
	}

	/**
	 * Call through to {@link DataPersister#generateId()}
	 */
	public Object generateId() {
		return dataPersister.generateId();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	/*public boolean isObjectsFieldValueDefault(Object object) throws SQLException {
		Object fieldValue = extractJavaFieldValue(object);
		return isFieldValueDefault(fieldValue);
	}*/


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

	/**
	 * Pass the foreign data argument to the foreign {@link Dao#create(Object)} method.
	 */
	public <T> int createWithForeignDao(T foreignData) throws SQLException {
		@SuppressWarnings("unchecked")
		Dao<T, ?> castDao = (Dao<T, ?>) foreignDao;
		return castDao.create(foreignData);
	}

	/*@Override
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != this.getClass()) {
			return false;
		}
		FieldType other = (FieldType) arg;
		return field.equals(other.field)
				&& (parentClass == null ? other.parentClass == null : parentClass.equals(other.parentClass));
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}*/

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":name=" + fieldName + ",type="
				+ dataType;
	}

	/*private Object createForeignObject(Object val, ObjectCache objectCache) throws SQLException {

		// try to stop the level counters objects from being created
		LevelCounters levelCounters = threadLevelCounters.get();
		if (levelCounters == null) {
			// only create a shell if we are not in auto-refresh mode
			if (!fieldConfig.isForeignAutoRefresh()) {
				return createForeignShell(val, objectCache);
			}
			levelCounters = new LevelCounters();
			threadLevelCounters.set(levelCounters);
		}

		// we record the current auto-refresh level which will be used along the way
		if (levelCounters.autoRefreshLevel == 0) {
			// if we aren't in an auto-refresh loop then don't _start_ an new loop if auto-refresh is not configured
			if (!fieldConfig.isForeignAutoRefresh()) {
				return createForeignShell(val, objectCache);
			}
			levelCounters.autoRefreshLevelMax = fieldConfig.getMaxForeignAutoRefreshLevel();
		}
		// if we have recursed the proper number of times, return a shell with just the id set
		if (levelCounters.autoRefreshLevel >= levelCounters.autoRefreshLevelMax) {
			return createForeignShell(val, objectCache);
		}

		*//*
		 * We may not have a mapped query for id because we aren't auto-refreshing ourselves. But a parent class may be
		 * auto-refreshing us with a level > 1 so we may need to build our query-for-id optimization on the fly here.
		 *//*
		if (mappedQueryForId == null) {
			@SuppressWarnings("unchecked")
			MappedQueryForId<Object, Object> castMappedQueryForId =
					(MappedQueryForId<Object, Object>) MappedQueryForId.build(connectionSource.getDatabaseType(),
							((BaseDaoImpl<?, ?>) foreignDao).getTableInfo(), foreignIdField);
			mappedQueryForId = castMappedQueryForId;
		}
		levelCounters.autoRefreshLevel++;
		try {
			DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
			try {
				// recurse and get the sub-object
				return mappedQueryForId.execute(databaseConnection, val, objectCache);
			} finally {
				connectionSource.releaseConnection(databaseConnection);
			}
		} finally {
			levelCounters.autoRefreshLevel--;
			if (levelCounters.autoRefreshLevel <= 0) {
				threadLevelCounters.remove();
			}
		}
	}*/

	/**
	 * Create a shell object and assign its id field.
	 */
	//TODO: foreign
	/*private Object createForeignShell(Object val) throws SQLException {
		Object foreignObject = foreignTableInfo.createObject();
		foreignIdField.assignField(foreignObject, val, false);
		return foreignObject;
	}*/

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	private boolean isFieldValueDefault(Object fieldValue) {
		if (fieldValue == null) {
			return true;
		} else {
			return fieldValue.equals(getJavaDefaultValueDefault());
		}
	}

	/**
	 * If we have a class Foo with a collection of Bar's then we go through Bar's DAO looking for a Foo field. We need
	 * this field to build the query that is able to find all Bar's that have foo_id that matches our id.
	 */
	/*private FieldType findForeignFieldType(Class<?> clazz, Class<?> foreignClass, BaseDaoImpl<?, ?> foreignDao)
			throws SQLException {
		String foreignColumnName = fieldConfig.getForeignCollectionForeignFieldName();
		for (FieldType fieldType : foreignDao.getTableInfo().getFieldTypes()) {
			if (fieldType.getType() == foreignClass
					&& (foreignColumnName == null || fieldType.getField().getName().equals(foreignColumnName))) {
				if (!fieldType.fieldConfig.isForeign() && !fieldType.fieldConfig.isForeignAutoRefresh()) {
					// this may never be reached
					throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
							+ "' contains a field of class " + foreignClass + " but it's not foreign");
				}
				return fieldType;
			}
		}
		// build our complex error message
		StringBuilder sb = new StringBuilder();
		sb.append("Foreign collection class ").append(clazz.getName());
		sb.append(" for field '").append(field.getName()).append("' column-name does not contain a foreign field");
		if (foreignColumnName != null) {
			sb.append(" named '").append(foreignColumnName).append('\'');
		}
		sb.append(" of class ").append(foreignClass.getName());
		throw new SQLException(sb.toString());
	}*/

	private void assignDataType(DatabaseType databaseType, DataPersister dataPersister, String defaultStr) throws SQLException {
		dataPersister = databaseType.getDataPersister(dataPersister, this);
		this.dataPersister = dataPersister;
		this.fieldConverter = databaseType.getFieldConverter(dataPersister, this);

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

		if (this.isId && !dataPersister.isAppropriateId()) {
			throw new SQLException("Field '" + fieldName + "' is of data type " + dataPersister
					+ " which cannot be the ID field");
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
