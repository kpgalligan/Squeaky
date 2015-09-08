package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.android.squeaky.SqueakyOpenHelper;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;

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
