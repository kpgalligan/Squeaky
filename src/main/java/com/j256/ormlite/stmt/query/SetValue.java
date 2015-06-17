package com.j256.ormlite.stmt.query;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.NullArgHolder;

import java.sql.SQLException;

public class SetValue extends BaseComparison
{

	/**
	 * Special value in case we are trying to set a field to null. We can't just use the null value because it looks
	 * like the argument has not been set in the base class.
	 */
	private static final ArgumentHolder nullValue = new NullArgHolder();

	public SetValue(String columnName, FieldType fieldType, Object value) throws SQLException {
		super(columnName, fieldType, (value == null ? nullValue : value), false);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("= ");
	}
}
