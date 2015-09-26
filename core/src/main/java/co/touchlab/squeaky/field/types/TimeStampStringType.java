package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Type that persists a {@link Timestamp} object as a String.
 *
 * @author graywatson
 */
public class TimeStampStringType extends DateStringType
{

	private static final TimeStampStringType singleTon = new TimeStampStringType();

	public static TimeStampStringType getSingleton()
	{
		return singleTon;
	}

	private TimeStampStringType()
	{
		super(SqlType.STRING);
	}

	/**
	 * Here for others to subclass.
	 */
	protected TimeStampStringType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		Date date = (Date) super.sqlArgToJava(fieldType, sqlArg, columnPos);
		return new Timestamp(date.getTime());
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject)
	{
		Timestamp timeStamp = (Timestamp) javaObject;
		return super.javaToSqlArg(fieldType, new Date(timeStamp.getTime()));
	}
}
