package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Type that persists a {@link BigInteger} object.
 *
 * @author graywatson
 */
public class BigIntegerType extends BaseDataType
{

	public static int DEFAULT_WIDTH = 255;

	private static final BigIntegerType singleTon = new BigIntegerType();

	public static BigIntegerType getSingleton()
	{
		return singleTon;
	}

	protected BigIntegerType()
	{
		super(SqlType.STRING, new Class<?>[]{BigInteger.class});
	}

	/**
	 * Here for others to subclass.
	 */
	protected BigIntegerType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		try
		{
			return new BigInteger(defaultStr).toString();
		}
		catch (IllegalArgumentException e)
		{
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default BigInteger string '"
					+ defaultStr + "'", e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		try
		{
			return new BigInteger((String) sqlArg);
		}
		catch (IllegalArgumentException e)
		{
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing BigInteger string '" + sqlArg
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj)
	{
		BigInteger bigInteger = (BigInteger) obj;
		return bigInteger.toString();
	}
}
