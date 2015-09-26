package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/**
 * Type that persists a String as a byte array.
 *
 * @author graywatson
 */
public class StringBytesType extends BaseDataType
{

	private static final String DEFAULT_STRING_BYTES_CHARSET_NAME = "Unicode";

	private static final StringBytesType singleTon = new StringBytesType();

	public static StringBytesType getSingleton()
	{
		return singleTon;
	}

	private StringBytesType()
	{
		super(SqlType.BYTE_ARRAY);
	}

	/**
	 * Here for others to subclass.
	 */
	protected StringBytesType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		throw new SQLException("String-bytes type cannot have default values");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getBlob(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		byte[] bytes = (byte[]) sqlArg;
		String charsetName = getCharsetName(fieldType);
		try
		{
			// NOTE: I can't use new String(bytes, Charset) because it was introduced in 1.6.
			return new String(bytes, charsetName);
		}
		catch (UnsupportedEncodingException e)
		{
			throw SqlExceptionUtil.create("Could not convert string with charset name: " + charsetName, e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException
	{
		String string = (String) javaObject;
		String charsetName = getCharsetName(fieldType);
		try
		{
			// NOTE: I can't use string.getBytes(Charset) because it was introduced in 1.6.
			return string.getBytes(charsetName);
		}
		catch (UnsupportedEncodingException e)
		{
			throw SqlExceptionUtil.create("Could not convert string with charset name: " + charsetName, e);
		}
	}

	private String getCharsetName(FieldType fieldType)
	{
		if (fieldType == null || fieldType.getFormat() == null)
		{
			return DEFAULT_STRING_BYTES_CHARSET_NAME;
		}
		else
		{
			return fieldType.getFormat();
		}
	}
}
