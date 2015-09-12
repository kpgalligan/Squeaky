package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;

/**
 * Internal class handling the SQL 'IS NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNull extends BaseComparison
{

	public IsNull(FieldType fieldType) throws SQLException {
		super(fieldType, null, false);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("IS NULL ");
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, StringBuilder sb) {
		// there is no value
	}
}
