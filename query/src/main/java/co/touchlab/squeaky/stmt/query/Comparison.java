package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;

import java.sql.SQLException;

/**
 * Internal interfaces which define a comparison operation.
 * 
 * @author graywatson
 */
interface Comparison extends Clause {

	/**
	 * Return the column-name associated with the comparison.
	 */
	String getColumnName();

	/**
	 * Add the operation used in this comparison to the string builder.
	 */
	void appendOperation(StringBuilder sb);

	/**
	 * Add the value of the comparison to the string builder.
	 */
	void appendValue(SqueakyContext squeakyContext, StringBuilder sb)
			throws SQLException;
}
