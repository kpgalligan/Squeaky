package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal interfaces which define a comparison operation.
 * 
 * @author graywatson
 */
interface Comparison extends Clause {

	/**
	 * Return the column-name associated with the comparison.
	 */
	public String getColumnName();

	/**
	 * Add the operation used in this comparison to the string builder.
	 */
	public void appendOperation(StringBuilder sb);

	/**
	 * Add the value of the comparison to the string builder.
	 */
	public void appendValue(SqueakyContext squeakyContext, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException;
}
