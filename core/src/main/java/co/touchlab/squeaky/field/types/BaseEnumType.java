package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Base class for the enum classes to provide a utility method.
 *
 * @author graywatson
 */
public abstract class BaseEnumType extends BaseDataType
{

	protected BaseEnumType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	protected BaseEnumType(SqlType sqlType)
	{
		super(sqlType);
	}

	protected static Enum<?> enumVal(FieldType fieldType, Object val, Enum<?> enumVal)
			throws SQLException
	{
		if (enumVal != null)
		{
			return enumVal;
		}
		else
		{
			throw new SQLException("Cannot get enum value of '" + val + "' for field " + fieldType);
		}
	}
}
