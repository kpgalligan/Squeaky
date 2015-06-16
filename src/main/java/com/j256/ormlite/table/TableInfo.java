package com.j256.ormlite.table;

import com.j256.ormlite.field.FieldType;

import java.sql.SQLException;
import java.util.Map;

/**
 * Information about a database table including the associated tableName, class, constructor, and the included fields.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class TableInfo<T, ID> {

	private static final FieldType[] NO_FOREIGN_COLLECTIONS = new FieldType[0];

	public final Class<T> dataClass;
	private final String tableName;
	private final FieldType[] fieldTypes;
	private final FieldType[] foreignCollections;
	public final FieldType idField;
	private final boolean foreignAutoCreate;
	private Map<String, FieldType> fieldNameMap;

	public TableInfo(DatabaseTableConfig<T> tableConfig) throws SQLException
	{
		this(tableConfig, null);
	}

	public TableInfo(DatabaseTableConfig<T> tableConfig, GeneratedTableMapper<T, ID> generatedTableMapper)
			throws SQLException {
		this.dataClass = tableConfig.getDataClass();
		this.tableName = tableConfig.getTableName();
		this.fieldTypes = tableConfig.getFieldTypes();
		// find the id field
		FieldType findIdFieldType = null;
		boolean foreignAutoCreate = false;
		int foreignCollectionCount = 0;
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.isId() || fieldType.isGeneratedId() ) {
				if (findIdFieldType != null) {
					throw new SQLException("More than 1 idField configured for class " + dataClass + " ("
							+ findIdFieldType + "," + fieldType + ")");
				}
				findIdFieldType = fieldType;
			}
			//TODO: foreign
			/*if (fieldType.isForeignAutoCreate()) {
				foreignAutoCreate = true;
			}
			if (fieldType.isForeignCollection()) {
				foreignCollectionCount++;
			}*/
		}
		// can be null if there is no id field
		this.idField = findIdFieldType;
		this.foreignAutoCreate = foreignAutoCreate;
		if (foreignCollectionCount == 0) {
			this.foreignCollections = NO_FOREIGN_COLLECTIONS;
		} else {
			this.foreignCollections = new FieldType[foreignCollectionCount];
			foreignCollectionCount = 0;
			for (FieldType fieldType : fieldTypes) {
				//TODO: foreign
				/*if (fieldType.isForeignCollection()) {
					this.foreignCollections[foreignCollectionCount] = fieldType;
					foreignCollectionCount++;
				}*/
			}
		}

	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Return the array of field types associated with the object.
	 */
	public FieldType[] getFieldTypes() {
		return fieldTypes;
	}


}
