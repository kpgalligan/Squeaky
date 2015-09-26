package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Boolean object.
 *
 * @author graywatson
 */
public class BooleanObjectType extends BaseDataType
{

	private static final BooleanObjectType singleTon = new BooleanObjectType();

	public static BooleanObjectType getSingleton()
	{
		return singleTon;
	}

	private BooleanObjectType()
	{
		super(SqlType.BOOLEAN, new Class<?>[]{Boolean.class});
	}

	protected BooleanObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	protected BooleanObjectType(SqlType sqlType)
	{
		super(sqlType);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Boolean.parseBoolean(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		if (results.isNull(columnPos) || results.getShort(columnPos) == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
