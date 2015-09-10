package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'NOT' boolean comparison operation. Used by {@link Where#not}.
 * 
 * @author graywatson
 */
public class Not<T> implements Clause, Queryable<T> {

	private final QueryFactory queryFactory;
	private Clause comparison = null;
	private final Queryable<T> parent;

	/**
	 * In this case we will consume a future clause.
	 */
	public Not(Queryable<T> parent, QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
		this.parent = parent;
	}

	public void appendSql(SqueakyContext squeakyContext, String tableName, StringBuilder sb,
			List<ArgumentHolder> selectArgList) throws SQLException {
		if (comparison == null) {
			throw new IllegalStateException("Clause has not been set in NOT operation");
		}
		// this generates: (NOT 'x' = 123 )
		sb.append("(NOT ");
		comparison.appendSql(squeakyContext, tableName, sb, selectArgList);

		sb.append(") ");
	}

	@Override
	public String toString() {
		if (comparison == null) {
			return "NOT without comparison";
		} else {
			return "NOT comparison " + comparison;
		}
	}

	@Override
	public String getStatement() throws SQLException
	{
		throw new SQLException("Must complete NOT statement");
	}

	@Override
	public List<T> query() throws SQLException
	{
		throw new SQLException("Must complete NOT statement");
	}

	@Override
	public List<T> query(String orderBy) throws SQLException
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
		comparison = queryFactory.eq(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> gt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.gt(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ge(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ge(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> lt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.lt(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> le(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.le(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> like(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.like(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> ne(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		comparison = queryFactory.ne(columnFieldName, value);
		return parent;
	}

	@Override
	public Queryable<T> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> in(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.in(columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		comparison = queryFactory.notIn(columnFieldName, objects);
		return parent;
	}

	@Override
	public Queryable<T> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		comparison = queryFactory.between(columnFieldName, low, high);
		return parent;
	}

	@Override
	public Queryable<T> isNull(String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNull(columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> isNotNull(String columnFieldName) throws SQLException
	{
		checkClause();
		comparison = queryFactory.isNotNull(columnFieldName);
		return parent;
	}

	@Override
	public Queryable<T> and() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(parent, queryFactory, ManyClause.AND_OPERATION);
		comparison = manyClause;
		return manyClause;
	}

	@Override
	public Queryable<T> or() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(parent, queryFactory, ManyClause.OR_OPERATION);
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
		if(comparison != null)
			throw new SQLException("Clause already defined. Must use and/or for multiple conditions");
	}
}
