package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.sql.SqlHelper;
import co.touchlab.squeaky.stmt.JoinAlias;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal base class for all comparison operations.
 *
 * @author graywatson
 */
abstract class BaseComparison implements Comparison
{

	private static final String NUMBER_CHARACTERS = "0123456789.-+";
	protected final FieldType fieldType;
	private final JoinAlias joinAlias;
	private final Object value;

	protected BaseComparison(FieldType fieldType, Object value, boolean isComparison, JoinAlias joinAlias)
			throws SQLException
	{
		this.joinAlias = joinAlias;
		if (isComparison && fieldType != null && !fieldType.isComparable())
		{
			throw new SQLException("Field '" + fieldType.getColumnName() + "' is of data type " + fieldType.getDataPersister()
					+ " which can not be compared");
		}
		this.fieldType = fieldType;
		this.value = value;
	}

	@Override
	public abstract String getOperation();

	public void appendSql(SqueakyContext squeakyContext, StringBuilder sb, boolean joinsAllowed)
			throws SQLException
	{

		SqlHelper.appendWhereClauseBody(sb, joinsAllowed ? joinAlias.tablePrefix : null, getOperation(), fieldType);
	}

	public void appendValue(SqueakyContext squeakyContext, List<String> params)
			throws SQLException
	{
		SqlHelper.appendArgOrValue(squeakyContext, fieldType, params, value);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fieldType.getColumnName()).append(' ');
		sb.append(getOperation());
		sb.append(' ');
		sb.append(value);
		return sb.toString();
	}
}
