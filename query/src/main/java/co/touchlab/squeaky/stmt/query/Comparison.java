package co.touchlab.squeaky.stmt.query;

/**
 * Internal interfaces which define a comparison operation.
 *
 * @author graywatson
 */
interface Comparison extends Clause
{

	/**
	 * Get the operation and param placeholder(s) for this comparison
	 */
	String getOperation();
}
