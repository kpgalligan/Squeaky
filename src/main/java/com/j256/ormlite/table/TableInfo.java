package com.j256.ormlite.table;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionInfo;

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
	private final String viewQuery;
	private final FieldType[] fieldTypes;
	private final ForeignCollectionInfo[] foreignCollections;
	public final FieldType idField;
	private final boolean foreignAutoCreate;
	private Map<String, FieldType> fieldNameMap;

	public TableInfo(Class clazz, String name, String viewQuery, FieldType[] fieldTypes, ForeignCollectionInfo[] foreignCollections)
			throws SQLException {
		this.dataClass = clazz;
		this.tableName = name;
		this.viewQuery = viewQuery;
		this.fieldTypes = fieldTypes;
		this.foreignCollections = foreignCollections;

		// find the id field
		FieldType findIdFieldType = null;
		boolean foreignAutoCreate = false;

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
			*/
		}
		// can be null if there is no id field
		this.idField = findIdFieldType;
		this.foreignAutoCreate = foreignAutoCreate;
	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName() {
		return tableName;
	}

	public String getViewQuery()
	{
		return viewQuery;
	}

	/**
	 * Return the array of field types associated with the object.
	 */
	public FieldType[] getFieldTypes() {
		return fieldTypes;
	}


}
