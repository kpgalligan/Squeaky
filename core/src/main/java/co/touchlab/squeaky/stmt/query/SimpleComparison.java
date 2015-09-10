package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;

import java.sql.SQLException;

/**
 * Internal class handling a simple comparison query part where the operation is passed in.
 * 
 * @author graywatson
 */
public class SimpleComparison extends BaseComparison
{

	public final static String EQUAL_TO_OPERATION = "=";
	public final static String GREATER_THAN_OPERATION = ">";
	public final static String GREATER_THAN_EQUAL_TO_OPERATION = ">=";
	public final static String LESS_THAN_OPERATION = "<";
	public final static String LESS_THAN_EQUAL_TO_OPERATION = "<=";
	public final static String LIKE_OPERATION = "LIKE";
	public final static String NOT_EQUAL_TO_OPERATION = "<>";

	private final String operation;

	public SimpleComparison(FieldType fieldType, Object value, String operation) throws SQLException {
		super(fieldType, value, true);
		this.operation = operation;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append(operation);
		sb.append(' ');
	}
}
