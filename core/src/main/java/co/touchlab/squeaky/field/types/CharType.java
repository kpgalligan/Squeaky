package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;

/**
 * Type that persists a char primitive.
 *
 * @author graywatson
 */
public class CharType extends CharacterObjectType
{

	private static final CharType singleTon = new CharType();

	public static CharType getSingleton()
	{
		return singleTon;
	}

	private CharType()
	{
		super(SqlType.CHAR, new Class<?>[]{char.class});
	}

	/**
	 * Here for others to subclass.
	 */
	protected CharType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject)
	{
		Character character = (Character) javaObject;
		// this is required because otherwise we try to store \0 in the database
		if (character == null || character == 0)
		{
			return null;
		}
		else
		{
			return character;
		}
	}

	@Override
	public boolean isPrimitive()
	{
		return true;
	}
}
