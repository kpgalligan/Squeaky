package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Byte object.
 *
 * @author graywatson
 */
public class ByteObjectType extends BaseDataType
{

	private static final ByteObjectType singleTon = new ByteObjectType();

	public static ByteObjectType getSingleton()
	{
		return singleTon;
	}

	private ByteObjectType()
	{
		super(SqlType.BYTE, new Class<?>[]{Byte.class});
	}

	protected ByteObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Byte.parseByte(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return (byte) results.getShort(columnPos);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
