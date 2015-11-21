package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.ModelDao;
import co.touchlab.squeaky.dao.Query;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.query.*;
import co.touchlab.squeaky.table.GeneratedTableMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Here's a page with a <a href="http://www.w3schools.com/Sql/" >good tutorial of SQL commands</a>.
 * </p>
 * <p/>
 * <p>
 * To create a query which looks up an account by name and password you would do the following:
 * </p>
 * <p/>
 * <pre>
 * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
 * Where where = qb.where();
 * // the name field must be equal to &quot;foo&quot;
 * where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;);
 * // and
 * where.and();
 * // the password field must be equal to &quot;_secret&quot;
 * where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;);
 * PreparedQuery&lt;Account, String&gt; preparedQuery = qb.prepareQuery();
 * </pre>
 * <p/>
 * <p>
 * In this example, the SQL query that will be generated will be approximately:
 * </p>
 * <p/>
 * <pre>
 * SELECT * FROM account WHERE (name = 'foo' AND passwd = '_secret')
 * </pre>
 * <p/>
 * <p>
 * If you'd rather chain the methods onto one line (like StringBuilder), this can also be written as:
 * </p>
 * <p/>
 * <pre>
 * queryBuilder.where().eq(Account.NAME_FIELD_NAME, &quot;foo&quot;).and().eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;);
 * </pre>
 * <p/>
 * <p>
 * If you'd rather use parens and the like then you can call:
 * </p>
 * <p/>
 * <pre>
 * Where where = queryBuilder.where();
 * where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;));
 * </pre>
 * <p/>
 * <p>
 * All three of the above call formats produce the same SQL. For complex queries that mix ANDs and ORs, the last format
 * will be necessary to get the grouping correct. For example, here's a complex query:
 * </p>
 * <p/>
 * <pre>
 * Where where = queryBuilder.where();
 * where.or(where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;)),
 * 		where.and(where.eq(Account.NAME_FIELD_NAME, &quot;bar&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;qwerty&quot;)));
 * </pre>
 * <p/>
 * <p>
 * This produces the following approximate SQL:
 * </p>
 * <p/>
 * <pre>
 * SELECT * FROM account WHERE ((name = 'foo' AND passwd = '_secret') OR (name = 'bar' AND passwd = 'qwerty'))
 * </pre>
 *
 * @author graywatson
 */
public class Where<T> implements Queryable<T>, Query
{
	private final static int CLAUSE_STACK_START_SIZE = 4;

	private final ModelDao<T> modelDao;
	private final SqueakyContext openHelperHelper;
	private final GeneratedTableMapper<T> generatedTableMapper;
	private final FieldType idFieldType;
	private final String idColumnName;
	private final QueryFactory queryFactory;
	private int joinTableCount = 0;
	private final List<JoinAlias> joins = new ArrayList<>();
	private final JoinAlias defaultJoinAlias;

	private Clause clause;

	public Where(Dao d) throws SQLException
	{
		if (!(d instanceof ModelDao))
			throw new SQLException("Dao must be a ModelDao instance");

		this.modelDao = (ModelDao<T>) d;
		this.openHelperHelper = modelDao.getOpenHelper();
		this.generatedTableMapper = modelDao.getGeneratedTableMapper();
		this.idFieldType = generatedTableMapper.getTableConfig().idField;
		this.queryFactory = new QueryFactory(openHelperHelper, modelDao.getDataClass());
		if (idFieldType == null)
		{
			this.idColumnName = null;
		}
		else
		{
			this.idColumnName = idFieldType.getColumnName();
		}
		this.defaultJoinAlias = new JoinAlias(this, ModelDao.DEFAULT_TABLE_PREFIX, modelDao.getDataClass(), ModelDao.DEFAULT_TABLE_PREFIX, null);
	}

	public Where<T> eq(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.eq(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> eq(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.eq(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> gt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.gt(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> gt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.gt(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> ge(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ge(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> ge(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ge(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> lt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.lt(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> lt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.lt(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> le(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.le(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> le(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.le(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> like(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.like(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> like(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.like(joinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> ne(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ne(defaultJoinAlias, columnFieldName, value);
		return this;
	}

	public Where<T> ne(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ne(joinAlias, columnFieldName, value);
		return this;
	}

	@Override
	public Where<T> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(defaultJoinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> in(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(joinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(defaultJoinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> notIn(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> in(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(defaultJoinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> in(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(joinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(defaultJoinAlias, columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T> notIn(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(joinAlias, columnFieldName, objects);
		return this;
	}

	public Where<T> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		clause = queryFactory.between(defaultJoinAlias, columnFieldName, low, high);
		return this;
	}

	public Where<T> between(JoinAlias joinAlias, String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		clause = queryFactory.between(joinAlias, columnFieldName, low, high);
		return this;
	}

	public Where<T> isNull(String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNull(defaultJoinAlias, columnFieldName);
		return this;
	}

	public Where<T> isNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNull(joinAlias, columnFieldName);
		return this;
	}

	public Where<T> isNotNull(String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNotNull(defaultJoinAlias, columnFieldName);
		return this;
	}

	public Where<T> isNotNull(JoinAlias joinAlias, String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNotNull(joinAlias, columnFieldName);
		return this;
	}

	public ManyClause and() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.AND_OPERATION, defaultJoinAlias);
		clause = manyClause;
		return manyClause;
	}

	public ManyClause or() throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.OR_OPERATION, defaultJoinAlias);
		clause = manyClause;
		return manyClause;
	}

	public Not not() throws SQLException
	{
		checkClause();
		Not<T> not = new Not<T>(this, queryFactory, defaultJoinAlias);
		clause = not;
		return not;
	}

	@Override
	public Queryable<T> end() throws SQLException
	{
		throw new SQLException("Where is not a child and can't be ended");
	}

	public JoinAlias join(String field) throws SQLException
	{
		return makeJoin(defaultJoinAlias.tablePrefix, field, modelDao.getDataClass());
	}

	public JoinAlias join(JoinAlias parent, String field) throws SQLException
	{
		return makeJoin(parent.tablePrefix, field, parent.tableType);
	}

	private JoinAlias makeJoin(String parentPrefix, String field, Class<T> dataClass) throws SQLException
	{
		FieldType fieldType = openHelperHelper.findFieldType(dataClass, field);
		if (fieldType.isForeign())
		{
			JoinAlias joinAlias = new JoinAlias(this, parentPrefix, fieldType.getFieldType(), "t" + (joinTableCount++), fieldType);
			joins.add(joinAlias);
			return joinAlias;
		}

		return null;
	}

	@Override
	public String getFromStatement(boolean joinsAllowed) throws SQLException
	{
		if (!joinsAllowed && joins.size() > 0)
			throw new SQLException("Joins not allowed for this operation");

		StringBuilder sb = new StringBuilder();
		sb.append(generatedTableMapper.getTableConfig().getTableName());
		sb.append(' ');
		if (joinsAllowed)
			sb.append(defaultJoinAlias.tablePrefix);


		for (JoinAlias join : joins)
		{
			sb.append(" join ");
			GeneratedTableMapper joinMapper = openHelperHelper.getGeneratedTableMapper(join.fieldType.getFieldType());
			sb.append(joinMapper.getTableConfig().getTableName());
			sb.append(' ');
			sb.append(join.tablePrefix);
			sb.append(" on ");
			sb.append(join.parentPrefix).append('.').append(join.fieldType.getColumnName());
			sb.append(" = ");
			sb.append(join.tablePrefix).append('.').append(joinMapper.getTableConfig().idField.getColumnName());
		}

		return sb.toString();
	}

	private void checkClause() throws SQLException
	{
		if (clause != null)
			throw new SQLException("Clause already defined. Must use and/or for multiple conditions");
	}

	public QueryFactory createQueryFactory()
	{
		return new QueryFactory(openHelperHelper, modelDao.getDataClass());
	}

	/**
	 * Reset the Where object so it can be re-used.
	 */
	public Where<T> reset()
	{
		clause = null;
		return this;
	}

	/**
	 * Returns the associated SQL WHERE statement.
	 */
	@Override
	public String getWhereStatement(boolean joinsAllowed) throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		appendSql(sb, joinsAllowed);
		return sb.toString();
	}

	@Override
	public String[] getParameters() throws SQLException
	{
		List<String> params = new ArrayList<>();
		clause.appendValue(openHelperHelper, params);
		return params.toArray(new String[params.size()]);
	}

	public Dao.QueryModifiers<T> query() throws SQLException
	{
		return modelDao.query(getWhereStatement(true), getParameters());
	}

	/**
	 * Used by the internal classes to add the where SQL to the {@link StringBuilder}.
	 */
	void appendSql(StringBuilder sb, boolean joinsAllowed) throws SQLException
	{
		clause.appendSql(openHelperHelper, sb, joinsAllowed);
	}

	@Override
	public String toString()
	{
		return "where clause: " + clause;
	}
}
