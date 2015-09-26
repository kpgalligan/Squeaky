package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a byte[] object.
 *
 * @author graywatson
 */
public class ByteArrayType extends BaseDataType
{

	private static final ByteArrayType singleTon = new ByteArrayType();

	public static ByteArrayType getSingleton()
	{
		return singleTon;
	}

	private ByteArrayType()
	{
		super(SqlType.BYTE_ARRAY);
	}

	/**
	 * Here for others to subclass.
	 */
	protected ByteArrayType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		throw new SQLException("byte[] type cannot have default values");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getBlob(columnPos);
	}
}
