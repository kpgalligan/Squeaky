package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Booleans can be stored in the database as the character '1' '0'. You can change the characters by specifying a format
 * string. It must be a string with 2 characters. The first character is the value for TRUE, the second is FALSE. You
 * must choose this DataType specifically with the {@link DatabaseField#dataType()} specifier.
 * <p/>
 * <pre>
 * &#64;DatabaseField(format = "YN", dataType = DataType.BOOLEAN_CHAR)
 * </pre>
 * <p/>
 * Thanks much to stew.
 *
 * @author graywatson
 */
public class BooleanCharType extends BooleanType
{

	private static final String DEFAULT_TRUE_FALSE_FORMAT = "10";

	private static final BooleanCharType singleTon = new BooleanCharType();

	public static BooleanCharType getSingleton()
	{
		return singleTon;
	}

	public BooleanCharType()
	{
		super(SqlType.STRING);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return javaToSqlArg(fieldType, Boolean.parseBoolean(defaultStr));
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj)
	{
		String format = (String) fieldType.getDataTypeConfigObj();
		return ((Boolean) obj ? format.charAt(0) : format.charAt(1));
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		String string = results.getString(columnPos);
		if (string == null || string.length() == 0)
		{
			return 0;
		}
		else if (string.length() == 1)
		{
			return string.charAt(0);
		}
		else
		{
			throw new SQLException("More than 1 character stored in database column: " + columnPos);
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
	{
		String format = (String) fieldType.getDataTypeConfigObj();
		return ((Character) sqlArg == format.charAt(0) ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException
	{
		String format = fieldType.getFormat();
		if (format == null)
		{
			return DEFAULT_TRUE_FALSE_FORMAT;
		}
		else if (format.length() == 2 && format.charAt(0) != format.charAt(1))
		{
			return format;
		}
		else
		{
			throw new SQLException(
					"Invalid boolean format must have 2 different characters that represent true/false like \"10\" or \"tf\": "
							+ format);
		}
	}
}
