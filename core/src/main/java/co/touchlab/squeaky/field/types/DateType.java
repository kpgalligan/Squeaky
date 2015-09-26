package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

/**
 * Type that persists a {@link java.util.Date} object.
 * <p/>
 * <p>
 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class that is handled by {@link SqlDateType}.
 * </p>
 *
 * @author graywatson
 */
public class DateType extends BaseDateType
{

	private static final DateType singleTon = new DateType();

	public static DateType getSingleton()
	{
		return singleTon;
	}

	private DateType()
	{
		super(SqlType.DATE, new Class<?>[]{java.util.Date.class});
	}

	protected DateType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		DateStringFormatConfig dateFormatConfig = convertDateStringConfig(fieldType, getDefaultDateFormatConfig());
		try
		{
			return new Timestamp(parseDateString(dateFormatConfig, defaultStr).getTime());
		}
		catch (ParseException e)
		{
			throw SqlExceptionUtil.create("Problems parsing default date string '" + defaultStr + "' using '"
					+ dateFormatConfig + '\'', e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		throw new SQLException("Android does not support timestamp.  Use JAVA_DATE_LONG or JAVA_DATE_STRING types");
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
	{
		Timestamp value = (Timestamp) sqlArg;
		return new java.util.Date(value.getTime());
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject)
	{
		java.util.Date date = (java.util.Date) javaObject;
		return new Timestamp(date.getTime());
	}

	/**
	 * Return the default date format configuration.
	 */
	protected DateStringFormatConfig getDefaultDateFormatConfig()
	{
		return defaultDateFormatConfig;
	}
}
