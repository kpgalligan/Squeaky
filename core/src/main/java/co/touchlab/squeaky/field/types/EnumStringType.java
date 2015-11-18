package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Type that persists an enum as its string value. You can also use the {@link EnumIntegerType}.
 *
 * @author graywatson
 */
public class EnumStringType extends BaseEnumType
{

	private static final EnumStringType singleTon = new EnumStringType();

	public static EnumStringType getSingleton()
	{
		return singleTon;
	}

	private EnumStringType()
	{
		super(SqlType.STRING, new Class<?>[]{Enum.class});
	}

	/**
	 * Here for others to subclass.
	 */
	protected EnumStringType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		if (fieldType == null)
		{
			return sqlArg;
		}
		String value = (String) sqlArg;
		@SuppressWarnings("unchecked")
		Map<String, Enum<?>> enumStringMap = (Map<String, Enum<?>>) fieldType.getDataTypeConfigObj();
		if (enumStringMap == null)
		{
			return enumVal(fieldType, value, null);
		}
		else
		{
			return enumVal(fieldType, value, enumStringMap.get(value));
		}
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return defaultStr;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj)
	{
		Enum<?> enumVal = (Enum<?>) obj;
		return enumVal.name();
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException
	{
		Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) fieldType.getFieldType().getEnumConstants();
		if (constants == null)
		{
			throw new SQLException("Field " + fieldType + " improperly configured as type " + this);
		}
		for (Enum<?> enumVal : constants)
		{
			enumStringMap.put(enumVal.name(), enumVal);
		}
		return enumStringMap;
	}
}
