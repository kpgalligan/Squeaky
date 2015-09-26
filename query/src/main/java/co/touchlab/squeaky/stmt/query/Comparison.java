package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal interfaces which define a comparison operation.
 * 
 * @author graywatson
 */
interface Comparison extends Clause {

	/**
	 * Get the operation and param placeholder(s) for this comparison
	 */
	String getOperation();
}
