package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Integer object.
 *
 * @author graywatson
 */
public class IntegerObjectType extends BaseDataType
{

	private static final IntegerObjectType singleTon = new IntegerObjectType();

	public static IntegerObjectType getSingleton()
	{
		return singleTon;
	}

	private IntegerObjectType()
	{
		super(SqlType.INTEGER, new Class<?>[]{Integer.class});
	}

	protected IntegerObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Integer.parseInt(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getInt(columnPos);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
