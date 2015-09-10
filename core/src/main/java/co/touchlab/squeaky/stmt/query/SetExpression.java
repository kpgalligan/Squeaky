package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

public class SetExpression extends BaseComparison
{

	public SetExpression(FieldType fieldType, String string) throws SQLException {
		super(fieldType, string, true);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("= ");
	}

	@Override
	protected void appendArgOrValue(SqueakyContext squeakyContext, FieldType fieldType, StringBuilder sb,
			List<ArgumentHolder> selectArgList, Object argOrValue) {
		// we know it is a string so just append it
		sb.append(argOrValue).append(' ');
	}
}
