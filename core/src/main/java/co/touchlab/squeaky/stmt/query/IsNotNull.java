package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.android.squeaky.SqueakyOpenHelper;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;
import co.touchlab.squeaky.stmt.Where;

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
