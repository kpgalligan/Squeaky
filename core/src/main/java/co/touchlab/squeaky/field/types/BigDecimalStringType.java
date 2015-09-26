package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Type that persists a {@link BigInteger} object.
 *
 * @author graywatson
 */
public class BigDecimalStringType extends BaseDataType
{

	public static int DEFAULT_WIDTH = 255;

	private static final BigDecimalStringType singleTon = new BigDecimalStringType();

	public static BigDecimalStringType getSingleton()
	{
		return singleTon;
	}

	private BigDecimalStringType()
	{
		super(SqlType.STRING, new Class<?>[]{BigDecimal.class});
	}

	/**
	 * Here for others to subclass.
	 */
	protected BigDecimalStringType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		try
		{
			return new BigDecimal(defaultStr).toString();
		}
		catch (IllegalArgumentException e)
		{
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default BigDecimal string '"
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
			return new BigDecimal((String) sqlArg);
		}
		catch (IllegalArgumentException e)
		{
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing BigDecimal string '" + sqlArg
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj)
	{
		BigDecimal bigInteger = (BigDecimal) obj;
		return bigInteger.toString();
	}
}
