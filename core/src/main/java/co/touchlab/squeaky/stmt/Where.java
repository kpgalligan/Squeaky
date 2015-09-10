package co.touchlab.squeaky.stmt;

import android.text.TextUtils;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.ModelDao;
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
 * 
 * <p>
 * To create a query which looks up an account by name and password you would do the following:
 * </p>
 * 
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
 * 
 * <p>
 * In this example, the SQL query that will be generated will be approximately:
 * </p>
 * 
 * <pre>
 * SELECT * FROM account WHERE (name = 'foo' AND passwd = '_secret')
 * </pre>
 * 
 * <p>
 * If you'd rather chain the methods onto one line (like StringBuilder), this can also be written as:
 * </p>
 * 
 * <pre>
 * queryBuilder.where().eq(Account.NAME_FIELD_NAME, &quot;foo&quot;).and().eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;);
 * </pre>
 * 
 * <p>
 * If you'd rather use parens and the like then you can call:
 * </p>
 * 
 * <pre>
 * Where where = queryBuilder.where();
 * where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;));
 * </pre>
 * 
 * <p>
 * All three of the above call formats produce the same SQL. For complex queries that mix ANDs and ORs, the last format
 * will be necessary to get the grouping correct. For example, here's a complex query:
 * </p>
 * 
 * <pre>
 * Where where = queryBuilder.where();
 * where.or(where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;)),
 * 		where.and(where.eq(Account.NAME_FIELD_NAME, &quot;bar&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;qwerty&quot;)));
 * </pre>
 * 
 * <p>
 * This produces the following approximate SQL:
 * </p>
 * 
 * <pre>
 * SELECT * FROM account WHERE ((name = 'foo' AND passwd = '_secret') OR (name = 'bar' AND passwd = 'qwerty'))
 * </pre>
 * 
 * @author graywatson
 */
public class Where<T, ID> implements Queryable<T> {

	private final static int CLAUSE_STACK_START_SIZE = 4;

	private final ModelDao<T, ID> modelDao;
	private final SqueakyContext openHelperHelper;
	private final GeneratedTableMapper<T, ID> generatedTableMapper;
	private final FieldType idFieldType;
	private final String idColumnName;
	private final QueryFactory queryFactory;

	private Clause clause;

	public Where(ModelDao<T, ID> modelDao) throws SQLException
	{
		this.modelDao = modelDao;
		this.openHelperHelper = modelDao.getOpenHelper();
		this.generatedTableMapper = modelDao.getGeneratedTableMapper();
		this.idFieldType = generatedTableMapper.getTableConfig().idField;
		this.queryFactory = new QueryFactory(openHelperHelper, modelDao.getDataClass());
		if (idFieldType == null) {
			this.idColumnName = null;
		} else {
			this.idColumnName = idFieldType.getColumnName();
		}
	}

	public Where<T, ID> eq(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.eq(columnFieldName, value);
		return this;
	}

	public Where<T, ID> gt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.gt(columnFieldName, value);
		return this;
	}

	public Where<T, ID> ge(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ge(columnFieldName, value);
		return this;
	}

	public Where<T, ID> lt(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.lt(columnFieldName, value);
		return this;
	}

	public Where<T, ID> le(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.le(columnFieldName, value);
		return this;
	}

	public Where<T, ID> like(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.like(columnFieldName, value);
		return this;
	}

	public Where<T, ID> ne(String columnFieldName, Object value) throws SQLException
	{
		checkClause();
		clause = queryFactory.ne(columnFieldName, value);
		return this;
	}

	@Override
	public Where<T, ID> in(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T, ID> notIn(String columnFieldName, Iterable<?> objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T, ID> in(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.in(columnFieldName, objects);
		return this;
	}

	@Override
	public Where<T, ID> notIn(String columnFieldName, Object... objects) throws SQLException
	{
		checkClause();
		clause = queryFactory.notIn(columnFieldName, objects);
		return this;
	}

	public Where<T, ID> between(String columnFieldName, Object low, Object high) throws SQLException
	{
		checkClause();
		clause = queryFactory.between(columnFieldName, low, high);
		return this;
	}

	public Where<T, ID> isNull(String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNull(columnFieldName);
		return this;
	}

	public Where<T, ID> isNotNull(String columnFieldName) throws SQLException
	{
		checkClause();
		clause = queryFactory.isNotNull(columnFieldName);
		return this;
	}

	public ManyClause and()throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.AND_OPERATION);
		clause = manyClause;
		return manyClause;
	}

	public ManyClause or()throws SQLException
	{
		checkClause();
		ManyClause<T> manyClause = new ManyClause<T>(this, queryFactory, ManyClause.OR_OPERATION);
		clause = manyClause;
		return manyClause;
	}

	public Not not()throws SQLException
	{
		checkClause();
		Not<T> not = new Not<T>(this, queryFactory);
		clause = not;
		return not;
	}

	@Override
	public Queryable<T> end() throws SQLException
	{
		throw new SQLException("Where is not a child and can't be ended");
	}

	private void checkClause() throws SQLException
	{
		if(clause != null)
			throw new SQLException("Clause already defined. Must use and/or for multiple conditions");
	}

	public QueryFactory createQueryFactory()
	{
		return new QueryFactory(openHelperHelper, modelDao.getDataClass());
	}

	/**
	 * Reset the Where object so it can be re-used.
	 */
	public Where<T, ID> reset() {
		clause = null;
		return this;
	}

	/**
	 * Returns the associated SQL WHERE statement.
	 */
	public String getStatement() throws SQLException {
		StringBuilder sb = new StringBuilder();
		appendSql(null, sb, new ArrayList<ArgumentHolder>());
		return sb.toString();
	}

	public List<T> query() throws SQLException
	{
		return modelDao.query(getStatement());
	}

	public List<T> query(String orderBy)throws SQLException
	{
		return modelDao.query(getStatement(), orderBy);
	}

	/**
	 * Used by the internal classes to add the where SQL to the {@link StringBuilder}.
	 *
	 * @param tableName
	 *            Name of the table to prepend to any column names or null to be ignored.
	 */
	void appendSql(String tableName, StringBuilder sb, List<ArgumentHolder> columnArgList) throws SQLException {
		clause.appendSql(openHelperHelper, tableName, sb, columnArgList);
	}

	@Override
	public String toString() {
		return "where clause: " + clause;
	}

	private FieldType findColumnFieldType(String columnName) throws SQLException
	{
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		for (FieldType fieldType : fieldTypes)
		{
			if(TextUtils.equals(fieldType.getColumnName(), columnName)
					||
					TextUtils.equals(fieldType.getFieldName(), columnName))
				return fieldType;
		}

		return null;
	}
}
