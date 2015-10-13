package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.stmt.JoinAlias;
import co.touchlab.squeaky.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'NOT' boolean comparison operation. Used by {@link Where#not}.
 *
 * @author graywatson
 */
public class Not<T> implements Clause, Queryable<T>
{

	private final QueryFactory queryFactory;
	private Clause comparison = null;
	private final Queryable<T> parent;
	private final JoinAlias joinAlias;

	/**
	 * In this case we will consume a future clause.
	 */
	public Not(Queryable<T> parent, QueryFactory queryFactory, JoinAlias joinAlias)
	{
		this.queryFactory = queryFactory;
		this.parent = parent;
		this.joinAlias = joinAlias;
	}

	@Override
	public void appendSql(SqueakyContext squeakyContext, StringBuilder sb, boolean joinsAllowed) throws SQLException
	{
		if (comparison == null)
		{
			throw new IllegalStateException("Clause has not been set in NOT operation");
		}
		// this generates: (NOT 'x' = 123 )
		sb.append("(NOT ");
		comparison.appendSql(squeakyContext, sb, joinsAllowed);

		sb.append(") ");
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, List<String> params) throws SQLException
	{
		comparison.appendValue(squeakyContext, params);
	}

	@Override
	public String toString()
	{
		if (comparison == null)
		{
			return "NOT without comparison";
		}
		else
		{
			return "NOT comparison " + comparison;
		}
	}

	@Override
	public String getWhereStatement(boolean joinsAllowed) throws SQLException
	{
		throw new SQLException("Must complete NOT statement");
	}

	@Override
	public Dao.QueryModifiers<T> query() throws SQLException
	{
		throw new SQLException("Must complete NOT statement");
	}

	@Override
	public Queryable<T> reset()
	{
		return parent.reset();
	}

	@Override
	public Queryable<T> eq(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.eq(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> eq(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.eq(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> gt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.gt(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> gt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.gt(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ge(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ge(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ge(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ge(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> lt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.lt(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> lt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.lt(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> le(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.le(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> le(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.le(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> like(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.like(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> like(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.like(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ne(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ne(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ne(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ne(joinAlias, columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> in(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> in(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> in(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		comparison = queryFactory.between(joinAlias, columnFieldName, low, high);
		return parent;
	}

	@Override
	public Queryable<T> between(JoinAlias joinAlias, String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		comparison = queryFactory.between(joinAlias, columnFieldName, low, high);
		return parent;
	}

	@Override
	public Queryable<T> isNull(String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNull(joinAlias, columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> isNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNull(joinAlias, columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> isNotNull(String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNotNull(joinAlias, columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> isNotNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNotNull(joinAlias, columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> and() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(parent, queryFactory, ManyClause.AND_OPERATION, joinAlias);
		comparison = manyClause;
		return manyClause;
	}

	@Override
	public Queryable<T> or() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(parent, queryFactory, ManyClause.OR_OPERATION, joinAlias);
		comparison = manyClause;
		return manyClause;
	}

	@Override
	public Queryable<T> not() throws SQLException
	{
		throw new SQLException("Double negative? Calling not on not");
	}

	@Override
	public Queryable<T> end() throws SQLException
	{
		throw new SQLException("Can't end a single entity");
	}

	private void checkClause() throws SQLException
	{
		if (comparison != null)
			throw new SQLException("Clause already defined. Must use and/or for multiple conditions");
	}
}
