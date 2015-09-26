package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Long object.
 *
 * @author graywatson
 */
public class LongObjectType extends BaseDataType
{

	private static final LongObjectType singleTon = new LongObjectType();

	public static LongObjectType getSingleton()
	{
		return singleTon;
	}

	private LongObjectType()
	{
		super(SqlType.LONG, new Class<?>[]{Long.class});
	}

	protected LongObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Long.parseLong(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getLong(columnPos);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
