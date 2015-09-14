package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.stmt.JoinAlias;

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
	private final Class defaultType;

	public ManyClause(Queryable<T> parent, QueryFactory queryFactory, String operation, Class defaultType) {
		this.parent = parent;
		this.defaultType = defaultType;
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
		clauses.add(queryFactory.eq(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> eq(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		clauses.add(queryFactory.eq(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> gt(String columnFieldName, Object value) throws SQLException
	{
		clauses.add(queryFactory.gt(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> gt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		clauses.add(queryFactory.gt(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> ge(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.ge(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> ge(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{

		clauses.add(queryFactory.ge(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> lt(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.lt(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> lt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{

		clauses.add(queryFactory.lt(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> le(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.le(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> le(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{

		clauses.add(queryFactory.le(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> like(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.like(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> like(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{

		clauses.add(queryFactory.like(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> ne(String columnFieldName, Object value) throws SQLException
	{
		
		clauses.add(queryFactory.ne(defaultType, columnFieldName, value));
		return this;
	}

	public ManyClause<T> ne(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{

		clauses.add(queryFactory.ne(joinAlias.tableType, columnFieldName, value));
		return this;
	}

	@Override
	public ManyClause<T> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.in(defaultType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> in(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.in(joinAlias.tableType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(defaultType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(joinAlias.tableType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> in(String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.in(defaultType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> in(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.in(joinAlias.tableType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(defaultType, columnFieldName, objects));
		return this;
	}

	@Override
	public ManyClause<T> notIn(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		clauses.add(queryFactory.notIn(joinAlias.tableType, columnFieldName, objects));
		return this;
	}

	public ManyClause<T> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		
		clauses.add(queryFactory.between(defaultType, columnFieldName, low, high));
		return this;
	}

	public ManyClause<T> between(JoinAlias joinAlias, String columnFieldName, Object low, Object high) throws SQLException
	{

		clauses.add(queryFactory.between(joinAlias.tableType, columnFieldName, low, high));
		return this;
	}

	public ManyClause<T> isNull(String columnFieldName) throws SQLException
	{
		
		clauses.add(queryFactory.isNull(defaultType, columnFieldName));
		return this;
	}

	public ManyClause<T> isNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{

		clauses.add(queryFactory.isNull(joinAlias.tableType, columnFieldName));
		return this;
	}

	public ManyClause<T> isNotNull(String columnFieldName) throws SQLException
	{
		
		clauses.add(queryFactory.isNotNull(defaultType, columnFieldName));
		return this;
	}

	public ManyClause<T> isNotNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{

		clauses.add(queryFactory.isNotNull(joinAlias.tableType, columnFieldName));
		return this;
	}

	public ManyClause<T> and()
	{
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.AND_OPERATION, defaultType);
		clauses.add(manyClause);
		return manyClause;
	}

	public ManyClause<T> or()
	{
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.OR_OPERATION, defaultType);
		clauses.add(manyClause);
		return manyClause;
	}

	public Not not()throws SQLException
	{
		Not<T> not = new Not<T>(this, queryFactory, defaultType);
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
