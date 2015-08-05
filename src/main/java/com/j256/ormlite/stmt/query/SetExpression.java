package com.j256.ormlite.stmt.query;

import com.j256.ormlite.android.squeaky.SqueakyOpenHelper;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

public class SetExpression extends BaseComparison
{

	public SetExpression(SqueakyOpenHelper openHelper, String columnName, FieldType fieldType, String string) throws SQLException {
		super(openHelper, columnName, fieldType, string, true);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("= ");
	}

	@Override
	protected void appendArgOrValue(FieldType fieldType, StringBuilder sb,
			List<ArgumentHolder> selectArgList, Object argOrValue) {
		// we know it is a string so just append it
		sb.append(argOrValue).append(' ');
	}
}
