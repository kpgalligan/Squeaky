package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * A custom persister that is able to store the org.joda.time.DateTime class in the database as epoch-millis long
 * integer. This is done with reflection so we don't have to introduce the dependency.
 * <p/>
 * <p>
 * <b>NOTE:</b> Because this class uses reflection, you have to specify this using {@link DatabaseField#dataType()}. It
 * won't be detected automatically.
 * </p>
 *
 * @author graywatson
 */
public class DateTimeType extends BaseDataType
{

	private static final DateTimeType singleTon = new DateTimeType();
	private static Class<?> dateTimeClass = null;
	private static Method getMillisMethod = null;
	private static Constructor<?> millisConstructor = null;
	private static final String[] associatedClassNames = new String[]{"org.joda.time.DateTime"};

	private DateTimeType()
	{
		super(SqlType.LONG);
	}

	protected DateTimeType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	public static DateTimeType getSingleton()
	{
		return singleTon;
	}

	@Override
	public String[] getAssociatedClassNames()
	{
		return associatedClassNames;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException
	{
		return extractMillis(javaObject);
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
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default DateTime value: "
					+ defaultStr, e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getLong(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		return createInstance((Long) sqlArg);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}

	private Object createInstance(Long sqlArg) throws SQLException
	{
		try
		{
			if (millisConstructor == null)
			{
				Class<?> clazz = getDateTimeClass();
				millisConstructor = clazz.getConstructor(long.class);
			}
			return millisConstructor.newInstance(sqlArg);
		}
		catch (Exception e)
		{
			throw SqlExceptionUtil.create("Could not use reflection to construct a Joda DateTime", e);
		}
	}

	private Long extractMillis(Object javaObject) throws SQLException
	{
		try
		{
			if (getMillisMethod == null)
			{
				Class<?> clazz = getDateTimeClass();
				getMillisMethod = clazz.getMethod("getMillis");
			}
			if (javaObject == null)
			{
				return null;
			}
			else
			{
				return (Long) getMillisMethod.invoke(javaObject);
			}
		}
		catch (Exception e)
		{
			throw SqlExceptionUtil.create("Could not use reflection to get millis from Joda DateTime: " + javaObject, e);
		}
	}

	private Class<?> getDateTimeClass() throws ClassNotFoundException
	{
		if (dateTimeClass == null)
		{
			dateTimeClass = Class.forName("org.joda.time.DateTime");
		}
		return dateTimeClass;
	}
}
