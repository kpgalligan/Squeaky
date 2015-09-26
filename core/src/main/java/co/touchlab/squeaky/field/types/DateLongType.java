package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.sql.SQLException;
import java.util.Date;

/**
 * Persists the {@link Date} Java class as long milliseconds since epoch.
 * <p/>
 * <p>
 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
 * </p>
 *
 * @author graywatson
 */
public class DateLongType extends BaseDateType
{

	private static final DateLongType singleTon = new DateLongType();

	public static DateLongType getSingleton()
	{
		return singleTon;
	}

	private DateLongType()
	{
		super(SqlType.LONG);
	}

	/**
	 * Here for others to subclass.
	 */
	protected DateLongType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		try
		{
			return Long.parseLong(defaultStr);
		}
		catch (NumberFormatException e)
		{
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default date-long value: "
					+ defaultStr, e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getLong(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
	{
		return new Date((Long) sqlArg);
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj)
	{
		Date date = (Date) obj;
		return date.getTime();
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
