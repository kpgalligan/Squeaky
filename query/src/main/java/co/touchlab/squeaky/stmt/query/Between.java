package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;

/**
 * Internal class handling the SQL 'between' query part. Used by {@link Where#between}.
 * 
 * @author graywatson
 */
public class Between extends BaseComparison
{
	private Object low;
	private Object high;

	public Between(FieldType fieldType, Object low, Object high) throws SQLException {
		super(fieldType, null, true);
		this.low = low;
		this.high = high;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("BETWEEN ");
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, StringBuilder sb)
			throws SQLException {
		if (low == null) {
			throw new IllegalArgumentException("BETWEEN low value for '" + fieldType.getColumnName() + "' is null");
		}
		if (high == null) {
			throw new IllegalArgumentException("BETWEEN high value for '" + fieldType.getColumnName() + "' is null");
		}
		appendArgOrValue(squeakyContext, fieldType, sb, low);
		sb.append("AND ");
		appendArgOrValue(squeakyContext, fieldType, sb, high);
	}
}
