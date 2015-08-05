package com.j256.ormlite.stmt.query;

import com.j256.ormlite.android.squeaky.SqueakyOpenHelper;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'IS NOT NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNotNull extends BaseComparison
{

	public IsNotNull(SqueakyOpenHelper openHelper, String columnName, FieldType fieldType) throws SQLException {
		super(openHelper, columnName, fieldType, null, false);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("IS NOT NULL ");
	}

	@Override
	public void appendValue(StringBuilder sb, List<ArgumentHolder> argList) {
		// there is no value
	}
}
