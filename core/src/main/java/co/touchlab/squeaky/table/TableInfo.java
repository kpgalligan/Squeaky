package co.touchlab.squeaky.table;

import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.FieldsEnum;
import co.touchlab.squeaky.field.ForeignCollectionInfo;

import java.sql.SQLException;

/**
 * Information about a database table including the associated tableName, class, constructor, and the included fields.
 *
 * @param <T> The class that the code will be operating on.
 * @author graywatson
 */
public class TableInfo<T>
{
	public final Class<T> dataClass;
	private final String tableName;
	private final FieldType[] fieldTypes;
	private final ForeignCollectionInfo[] foreignCollections;
	public final FieldType idField;

	public TableInfo(Class clazz, String name, FieldsEnum[] fields, ForeignCollectionInfo[] foreignCollections)
			throws SQLException
	{
		this.dataClass = clazz;
		this.tableName = name;
		this.fieldTypes = new FieldType[fields.length];
		int i = 0;
		for (FieldsEnum field : fields)
		{
			fieldTypes[i++] = field.getFieldType();
		}
		this.foreignCollections = foreignCollections;

		// find the id field
		FieldType findIdFieldType = null;

		for (FieldType fieldType : fieldTypes)
		{
			if (fieldType.isId() || fieldType.isGeneratedId())
			{
				if (findIdFieldType != null)
				{
					throw new SQLException("More than 1 idField configured for class " + dataClass + " ("
							+ findIdFieldType + "," + fieldType + ")");
				}
				findIdFieldType = fieldType;
			}
		}

		// can be null if there is no id field
		this.idField = findIdFieldType;
	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Return the array of field types associated with the object.
	 */
	public FieldType[] getFieldTypes()
	{
		return fieldTypes;
	}

	public ForeignCollectionInfo[] getForeignCollections()
	{
		return foreignCollections;
	}
}
