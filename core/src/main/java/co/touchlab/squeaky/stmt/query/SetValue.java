package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;
import co.touchlab.squeaky.stmt.NullArgHolder;

import java.sql.SQLException;

public class SetValue extends BaseComparison
{

	/**
	 * Special value in case we are trying to set a field to null. We can't just use the null value because it looks
	 * like the argument has not been set in the base class.
	 */
	private static final ArgumentHolder nullValue = new NullArgHolder();

	public SetValue(SqueakyContext openHelper, String columnName, FieldType fieldType, Object value) throws SQLException {
		super(openHelper, columnName, fieldType, (value == null ? nullValue : value), false);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("= ");
	}
}
