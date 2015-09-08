package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal marker class for query clauses.
 * 
 * @author graywatson
 */
public interface Clause {

	/**
	 * Add to the string-builder the appropriate SQL for this clause.
	 * 
	 * @param tableName
	 *            Name of the table to prepend to any column names or null to be ignored.
	 */
	void appendSql(SqueakyContext squeakyContext, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException;
}
