package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * For operations with a number of them in a row.
 * 
 * @author graywatson
 */
public class ManyClause<T> implements Clause, Queryable<T> {

	public static final String AND_OPERATION = "AND";
	public static final String OR_OPERATION = "OR";

	private final List<Clause> clauses;
	private final String operation;
	private final QueryFactory queryFactory;
	private final Queryable<T> parent;

	public ManyClause(Queryable<T> parent, QueryFactory queryFactory, String operation) {
		this.parent = parent;
		this.clauses = new ArrayList<>(2);
		this.operation = operation;
		this.queryFactory = queryFactory;
	}

	@Override
	public String getStatement() throws SQLException
	{
		return parent.getStatement();
	}

	@Override
	public List<T> query() throws SQLException
	{
		return parent.query();
	}

	@Override
	public List<T> query(String orderBy) throws SQLException
	{
		return parent.query(orderBy);
	}

	@Override
	public Queryable<T> reset()
	{
		return parent.reset();
	}

	public ManyClause<T> eq(String columnFieldName, Object value) throws SQLException
	{
		clauses.add(queryFactory.eq(columnFieldName, value));
		return this;
	}

	public ManyClause<T> gt(String columnFieldName, Object value) throws SQLException
	{
		clauses.add(queryFactory.gt(columnFieldName, value));
		return this;
	}

	public ManyClause<T> ge(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.ge(columnFieldName, value));
		return this;
	}

	public ManyClause<T> lt(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.lt(columnFieldName, value));
		return this;
	}

	public ManyClause<T> le(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.le(columnFieldName, value));
		return this;
	}

	public ManyClause<T> like(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.like(columnFieldName, value));
		return this;
	}

	public ManyClause<T> ne(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.ne(columnFieldName, value));
		return this;
	}

	@Override
	public ManyClause<T> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.in(columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> in(String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.in(columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(columnFieldName, objects));
		return this;
	}

	public ManyClause<T> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		
		clauses.add(queryFactory.between(columnFieldName, low, high));
		return this;
	}

	public ManyClause<T> isNull(String columnFieldName) throws SQLException
	{
		
		clauses.add(queryFactory.isNull(columnFieldName));
		return this;
	}

	public ManyClause<T> isNotNull(String columnFieldName) throws SQLException
	{
		
		clauses.add(queryFactory.isNotNull(columnFieldName));
		return this;
	}

	public ManyClause<T> and()
	{
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.AND_OPERATION);
		clauses.add(manyClause);
		return manyClause;
	}

	public ManyClause<T> or()
	{
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.OR_OPERATION);
		clauses.add(manyClause);
		return manyClause;
	}

	public Not not()throws SQLException
	{
		Not<T> not = new Not<T>(this, queryFactory);
		clauses.add(not);
		return not;
	}

	@Override
	public Queryable<T> end()throws SQLException
	{
		return parent;
	}

	public void appendSql(SqueakyContext squeakyContext, String tableName, StringBuilder sb) throws SQLException {

		if(clauses.size() == 0)
			throw new SQLException("Clause list can't be empty for "+ operation);

		boolean first = true;

		for (Clause clause : clauses)
		{
			if(first)
			{
				first = false;
				sb.append("(");
			}
			else
			{
				sb.append(' ');
				sb.append(operation);
				sb.append(' ');
			}

			clause.appendSql(squeakyContext, tableName, sb);
		}

		sb.append(") ");
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, List<String> params) throws SQLException
	{
		for (Clause clause : clauses)
		{
			clause.appendValue(squeakyContext, params);
		}
	}
}
