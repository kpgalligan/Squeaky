package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a boolean primitive.
 *
 * @author graywatson
 */
public class FloatObjectType extends BaseDataType
{

	private static final FloatObjectType singleTon = new FloatObjectType();

	public static FloatObjectType getSingleton()
	{
		return singleTon;
	}

	private FloatObjectType()
	{
		super(SqlType.FLOAT, new Class<?>[]{Float.class});
	}

	protected FloatObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getFloat(columnPos);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Float.parseFloat(defaultStr);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
