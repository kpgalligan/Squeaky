package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.JoinAlias;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'IS NULL' comparison query part. Used by {@link Where#isNull}.
 *
 * @author graywatson
 */
public class IsNull extends BaseComparison
{
	public IsNull(FieldType fieldType, JoinAlias joinAlias) throws SQLException
	{
		super(fieldType, null, false, joinAlias);
	}

	@Override
	public String getOperation()
	{
		return "IS NULL";
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, List<String> params)
	{
		// there is no value
	}
}
