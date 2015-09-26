package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal marker class for query clauses.
 *
 * @author graywatson
 */
public interface Clause
{

	/**
	 * Add to the string-builder the appropriate SQL for this clause.
	 */
	void appendSql(SqueakyContext squeakyContext, StringBuilder sb, boolean joinsAllowed)
			throws SQLException;

	void appendValue(SqueakyContext squeakyContext, List<String> params)
			throws SQLException;
}
