package com.j256.ormlite.field.types;

import android.database.Cursor;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

import java.sql.SQLException;

/**
 * Type that persists a Character object.
 * 
 * @author graywatson
 */
public class CharacterObjectType extends BaseDataType {

	private static final CharacterObjectType singleTon = new CharacterObjectType();

	public static CharacterObjectType getSingleton() {
		return singleTon;
	}

	private CharacterObjectType() {
		super(SqlType.CHAR, new Class<?>[] { Character.class });
	}

	protected CharacterObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		if (defaultStr.length() != 1) {
			throw new SQLException("Problems with field " + fieldType + ", default string to long for Character: '"
					+ defaultStr + "'");
		}
		return (Character) defaultStr.charAt(0);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException {
		String string = results.getString(columnPos);
		if (string == null || string.length() == 0) {
			return 0;
		} else if (string.length() == 1) {
			return string.charAt(0);
		} else {
			throw new SQLException("More than 1 character stored in database column: " + columnPos);
		}
	}
}
