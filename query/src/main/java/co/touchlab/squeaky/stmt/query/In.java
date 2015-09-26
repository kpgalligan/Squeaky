package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.sql.SqlHelper;
import co.touchlab.squeaky.stmt.JoinAlias;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Internal class handling the SQL 'in' query part. Used by {@link Where#in}.
 *
 * @author graywatson
 */
public class In extends BaseComparison
{
	private Collection<?> objects;
	private final boolean in;

	public In(FieldType fieldType, Collection<?> objects, boolean in, JoinAlias joinAlias) throws SQLException
	{
		super(fieldType, null, true, joinAlias);
		this.objects = objects;
		this.in = in;
	}

	public In(FieldType fieldType, Object[] objects, boolean in, JoinAlias joinAlias) throws SQLException
	{
		super(fieldType, null, true, joinAlias);
		// grrrr, Object[] should be Iterable
		this.objects = Arrays.asList(objects);
		this.in = in;
	}

	@Override
	public String getOperation()
	{
		StringBuilder sb = new StringBuilder();
		if (in)
		{
			sb.append("IN (");
		}
		else
		{
			sb.append("NOT IN (");
		}
		for (int i = 0; i < objects.size(); i++)
		{
			if (i > 0)
				sb.append(',');

			sb.append('?');
		}
		sb.append(')');

		return sb.toString();
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, List<String> params)
			throws SQLException
	{
		for (Object value : objects)
		{
			if (value == null)
			{
				throw new IllegalArgumentException("one of the IN values for '" + fieldType.getColumnName() + "' is null");
			}
			SqlHelper.appendArgOrValue(squeakyContext, fieldType, params, value);
		}
	}
}
