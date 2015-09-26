package co.touchlab.squeaky.field;

import co.touchlab.squeaky.field.types.BaseDataType;

import java.sql.SQLException;

/**
 * Data type that provide Java class to/from database mapping.
 * <p/>
 * <p>
 * If you are defining your own custom persister, then chances are you should extend {@link BaseDataType}. See
 * {@link DatabaseField#persisterClass()}.
 * </p>
 *
 * @author graywatson
 */
public interface DataPersister extends FieldConverter
{

	/**
	 * Return the classes that should be associated with this.
	 */
	Class<?>[] getAssociatedClasses();

	/**
	 * Return the class names that should be associated with this or null. This is used by reflection classes so we can
	 * discover if a Field matches _without_ needed the class dependency in -core.
	 */
	String[] getAssociatedClassNames();

	/**
	 * This makes a configuration object for the data-type or returns null if none. The object can be accessed later via
	 * {@link FieldType#getDataTypeConfigObj()}.
	 */
	Object makeConfigObject(FieldType fieldType) throws SQLException;

	/**
	 * Return whether this field's default value should be escaped in SQL.
	 */
	boolean isEscapedDefaultValue();

	/**
	 * Return whether we need to escape this value in SQL expressions. Numbers _must_ not be escaped but most other
	 * values should be.
	 */
	boolean isEscapedValue();

	/**
	 * Return whether this field is a primitive type or not. This is used to know if we should throw if the field value
	 * is null.
	 */
	boolean isPrimitive();

	/**
	 * Return true if this data type be compared in SQL statements.
	 */
	boolean isComparable();
}
