package co.touchlab.squeaky.field;

import co.touchlab.squeaky.field.types.VoidType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Annotation that identifies a field in a class that corresponds to a column in the database and will be persisted.
 * Fields that are not to be persisted such as transient or other temporary fields probably should be ignored. For
 * example:
 * </p>
 * <p/>
 * <pre>
 * &#064;DatabaseField(id = true)
 * private String name;
 *
 * &#064;DatabaseField(columnName = &quot;passwd&quot;, canBeNull = false)
 * private String password;
 * </pre>
 * <p/>
 * <p>
 * <b> WARNING:</b> If you add any extra fields here, you will need to add them to {@link DatabaseFieldConfig},
 * {@link DatabaseFieldConfigLoader}, DatabaseFieldConfigLoaderTest, and DatabaseTableConfigUtil as well.
 * </p>
 *
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DatabaseField
{

	/**
	 * this special string is used as a .equals check to see if no default was specified
	 */
	String DEFAULT_STRING = "__ormlite__ no default value string was specified";

	/**
	 * The name of the column in the database. If not set then the name is taken from the field name.
	 */
	String columnName() default "";

	/**
	 * The DataType associated with the field. If not set then the Java class of the field is used to match with the
	 * appropriate DataType. This should only be set if you are overriding the default database type or if the field
	 * cannot be automatically determined (ex: byte[]).
	 */
	DataType dataType() default DataType.UNKNOWN;

	/**
	 * The default value of the field for creating the table. Default is none.
	 * <p/>
	 * <p>
	 * <b>NOTE:</b> If the field has a null value then this value will be inserted in its place when you call you call
	 * {@link Dao#create(Object)}. This does not apply to primitive fields so you should just assign them in the class
	 * instead.
	 * </p>
	 */
	String defaultValue() default DEFAULT_STRING;

	/**
	 * Whether the field can be assigned to null or have no value. Default is true.
	 */
	boolean canBeNull() default true;

	boolean id() default false;

	boolean generatedId() default false;

	boolean foreign() default false;

	/**
	 * <p>
	 * Package should use get...() and set...() to access the field value instead of the default direct field access via
	 * reflection. This may be necessary if the object you are storing has protections around it.
	 * </p>
	 * <p/>
	 * <p>
	 * <b>NOTE:</b> The name of the get method <i>must</i> match getXxx() where Xxx is the name of the field with the
	 * first letter capitalized. The get <i>must</i> return a class which matches the field's. The set method
	 * <i>must</i> match setXxx(), have a single argument whose class matches the field's, and return void. For example:
	 * </p>
	 * <p/>
	 * <pre>
	 * &#064;DatabaseField
	 * private Integer orderCount;
	 *
	 * public Integer getOrderCount() {
	 * 	return orderCount;
	 * }
	 *
	 * public void setOrderCount(Integer orderCount) {
	 * 	this.orderCount = orderCount;
	 * }
	 * </pre>
	 */
	boolean useGetSet() default false;

	/**
	 * Optional format information that can be used by various field types. For example, if the Date is to be persisted
	 * as a string, this can set what format string to use for the date.
	 */
	String format() default "";

	/**
	 * Set this to be true (default false) to have the database insure that the column is unique to all rows in the
	 * table. Use this when you wan a field to be unique even if it is not the identify field. For example, if you have
	 * the firstName and lastName fields, both with unique=true and you have "Bob", "Smith" in the database, you cannot
	 * insert either "Bob", "Jones" or "Kevin", "Smith".
	 */
	boolean unique() default false;

	/**
	 * Set this to be true (default false) to have the database insure that _all_ of the columns marked with this as
	 * true will together be unique. For example, if you have the firstName and lastName fields, both with unique=true
	 * and you have "Bob", "Smith" in the database, you cannot insert another "Bob", "Smith" but you can insert "Bob",
	 * "Jones" and "Kevin", "Smith".
	 */
	boolean uniqueCombo() default false;

	/**
	 * Set this to be true (default false) to have the database add an index for this field. This will create an index
	 * with the name columnName + "_idx". To specify a specific name of the index or to index multiple fields, use
	 * {@link #indexName()}.
	 */
	boolean index() default false;

	/**
	 * Set this to be true (default false) to have the database add a unique index for this field. This is the same as
	 * the {@link #index()} field but this ensures that all of the values in the index are unique..
	 */
	boolean uniqueIndex() default false;

	/**
	 * Set this to be a string (default none) to have the database add an index for this field with this name. You do
	 * not need to specify the {@link #index()} boolean as well. To index multiple fields together in one index, each of
	 * the fields should have the same indexName value.
	 */
	String indexName() default "";

	/**
	 * Set this to be a string (default none) to have the database add a unique index for this field with this name.
	 * This is the same as the {@link #indexName()} field but this ensures that all of the values in the index are
	 * unique.
	 */
	String uniqueIndexName() default "";

	/**
	 * Set this to be true (default false) to have a foreign field automagically refreshed when an object is queried.
	 * This will _not_ automagically create the foreign object but when the object is queried, a separate database call
	 * will be made to load of the fields of the foreign object via an internal DAO. The default is to just have the ID
	 * field in the object retrieved and for the caller to call refresh on the correct DAO.
	 *
	 * This will only traverse 2 levels by default.  Also, this will be ignored if you specify a refresh map explicitly.
	 */
	boolean foreignAutoRefresh() default false;

	/**
	 * Allows you to set a custom persister class to handle this field. This class must have a getSingleton() static
	 * method defined which will return the singleton persister.
	 *
	 * @see DataPersister
	 */
	Class<? extends DataPersister> persisterClass() default VoidType.class;
}
