package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Short object.
 *
 * @author graywatson
 */
public class ShortObjectType extends BaseDataType
{

	private static final ShortObjectType singleTon = new ShortObjectType();

	public static ShortObjectType getSingleton()
	{
		return singleTon;
	}

	private ShortObjectType()
	{
		super(SqlType.SHORT, new Class<?>[]{Short.class});
	}

	protected ShortObjectType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return Short.parseShort(defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getShort(columnPos);
	}

	@Override
	public boolean isEscapedValue()
	{
		return false;
	}
}
