package co.touchlab.squeaky.field;

import java.sql.SQLException;

/**
 * Base class for field-converters.
 *
 * @author graywatson
 */
public abstract class BaseFieldConverter implements FieldConverter
{

	/**
	 * @throws SQLException If there are problems with the conversion.
	 */
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException
	{
		// noop pass-thru
		return javaObject;
	}

	/**
	 * @throws SQLException If there are problems with the conversion.
	 */
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		// noop pass-thru
		return sqlArg;
	}
}
