package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.android.squeaky.SqueakyOpenHelper;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'between' query part. Used by {@link Where#between}.
 * 
 * @author graywatson
 */
public class Between extends BaseComparison
{
	private Object low;
	private Object high;

	public Between(SqueakyOpenHelper openHelper, String columnName, FieldType fieldType, Object low, Object high) throws SQLException {
		super(openHelper, columnName, fieldType, null, true);
		this.low = low;
		this.high = high;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("BETWEEN ");
	}

	@Override
	public void appendValue(StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		if (low == null) {
			throw new IllegalArgumentException("BETWEEN low value for '" + columnName + "' is null");
		}
		if (high == null) {
			throw new IllegalArgumentException("BETWEEN high value for '" + columnName + "' is null");
		}
		appendArgOrValue(fieldType, sb, argList, low);
		sb.append("AND ");
		appendArgOrValue(fieldType, sb, argList, high);
	}
}
