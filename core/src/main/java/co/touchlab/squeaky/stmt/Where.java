package co.touchlab.squeaky.stmt;

import android.text.TextUtils;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.ModelDao;
import co.touchlab.squeaky.dao.SqueakyOpenHelper;
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
public class Where<T, ID> {

	private final static int CLAUSE_STACK_START_SIZE = 4;

	private final ModelDao<T, ID> modelDao;
	private final SqueakyOpenHelper openHelper;
	private final GeneratedTableMapper<T, ID> generatedTableMapper;
	private final FieldType idFieldType;
	private final String idColumnName;

	private Clause[] clauseStack = new Clause[CLAUSE_STACK_START_SIZE];
	private int clauseStackLevel;
	private NeedsFutureClause needsFuture = null;

	public Where(ModelDao<T, ID> modelDao) throws SQLException
	{
		this.modelDao = modelDao;
		this.openHelper = modelDao.getOpenHelper();
		this.generatedTableMapper = modelDao.getGeneratedTableMapper();
		this.idFieldType = generatedTableMapper.getTableConfig().idField;
		if (idFieldType == null) {
			this.idColumnName = null;
		} else {
			this.idColumnName = idFieldType.getColumnName();
		}
	}

	/**
	 * AND operation which takes the previous clause and the next clause and AND's them together.
	 */
	public Where<T, ID> and() {
		ManyClause clause = new ManyClause(pop("AND"), ManyClause.AND_OPERATION);
		push(clause);
		addNeedsFuture(clause);
		return this;
	}

	/**
	 * AND operation which takes 2 (or more) arguments and AND's them together.
	 *
	 * <p>
	 * <b>NOTE:</b> There is no guarantee of the order of the clauses that are generated in the final query.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> I can't remove the generics code warning that can be associated with this method. You can instead
	 * use the {@link #and(int)} method.
	 * </p>
	 */
	public Where<T, ID> and(Where<T, ID> first, Where<T, ID> second, Where<T, ID>... others) {
		Clause[] clauses = buildClauseArray(others, "AND");
		Clause secondClause = pop("AND");
		Clause firstClause = pop("AND");
		addClause(new ManyClause(firstClause, secondClause, clauses, ManyClause.AND_OPERATION));
		return this;
	}

	/**
	 * This method needs to be used carefully. This will absorb a number of clauses that were registered previously with
	 * calls to {@link Where#eq(String, Object)} or other methods and will string them together with AND's. There is no
	 * way to verify the number of previous clauses so the programmer has to count precisely.
	 *
	 * <p>
	 * <b>NOTE:</b> There is no guarantee of the order of the clauses that are generated in the final query.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b> This will throw an exception if numClauses is 0 but will work with 1 or more.
	 * </p>
	 */
	public Where<T, ID> and(int numClauses) {
		if (numClauses == 0) {
			throw new IllegalArgumentException("Must have at least one clause in and(numClauses)");
		}
		Clause[] clauses = new Clause[numClauses];
		for (int i = numClauses - 1; i >= 0; i--) {
			clauses[i] = pop("AND");
		}
		addClause(new ManyClause(clauses, ManyClause.AND_OPERATION));
		return this;
	}

	/**
	 * Add a BETWEEN clause so the column must be between the low and high parameters.
	 */
	public Where<T, ID> between(String columnName, Object low, Object high) throws SQLException
	{

		addClause(new Between(openHelper, columnName, findColumnFieldType(columnName), low, high));
		return this;
	}

	/**
	 * Add a '=' clause so the column must be equal to the value.
	 */
	public Where<T, ID> eq(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Add a '&gt;=' clause so the column must be greater-than or equals-to the value.
	 */
	public Where<T, ID> ge(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.GREATER_THAN_EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Add a '&gt;' clause so the column must be greater-than the value.
	 */
	public Where<T, ID> gt(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.GREATER_THAN_OPERATION));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects from the list passed in.
	 */
	public Where<T, ID> in(String columnName, Iterable<?> objects) throws SQLException {
		addClause(new In(openHelper, columnName, findColumnFieldType(columnName), objects, true));
		return this;
	}

	/**
	 * Same as {@link #in(String, Iterable)} except with a NOT IN clause.
	 */
	public Where<T, ID> notIn(String columnName, Iterable<?> objects) throws SQLException {
		addClause(new In(openHelper, columnName, findColumnFieldType(columnName), objects, false));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects passed in.
	 */
	public Where<T, ID> in(String columnName, Object... objects) throws SQLException {
		return in(true, columnName, objects);
	}

	/**
	 * Same as {@link #in(String, Object...)} except with a NOT IN clause.
	 */
	public Where<T, ID> notIn(String columnName, Object... objects) throws SQLException {
		return in(false, columnName, objects);
	}

	/**
	 * Add a 'IS NULL' clause so the column must be null. '=' NULL does not work.
	 */
	public Where<T, ID> isNull(String columnName) throws SQLException {
		addClause(new IsNull(openHelper, columnName, findColumnFieldType(columnName)));
		return this;
	}

	/**
	 * Add a 'IS NOT NULL' clause so the column must not be null. '&lt;&gt;' NULL does not work.
	 */
	public Where<T, ID> isNotNull(String columnName) throws SQLException {
		addClause(new IsNotNull(openHelper, columnName, findColumnFieldType(columnName)));
		return this;
	}

	/**
	 * Add a '&lt;=' clause so the column must be less-than or equals-to the value.
	 */
	public Where<T, ID> le(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.LESS_THAN_EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Add a '&lt;' clause so the column must be less-than the value.
	 */
	public Where<T, ID> lt(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.LESS_THAN_OPERATION));
		return this;
	}

	/**
	 * Add a LIKE clause so the column must mach the value using '%' patterns.
	 */
	public Where<T, ID> like(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.LIKE_OPERATION));
		return this;
	}

	/**
	 * Add a '&lt;&gt;' clause so the column must be not-equal-to the value.
	 */
	public Where<T, ID> ne(String columnName, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value,
				SimpleComparison.NOT_EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Used to NOT the next clause specified.
	 */
	public Where<T, ID> not() {
		/*
		 * Special circumstance here when we have a needs future with a not. Something like and().not().like(...). In
		 * this case we satisfy the and()'s future as the not() but the not() becomes the new needs-future.
		 */
		Not not = new Not();
		addClause(not);
		addNeedsFuture(not);
		return this;
	}

	/**
	 * Used to NOT the argument clause specified.
	 */
	public Where<T, ID> not(Where<T, ID> comparison) {
		addClause(new Not(pop("NOT")));
		return this;
	}

	/**
	 * OR operation which takes the previous clause and the next clause and OR's them together.
	 */
	public Where<T, ID> or() {
		ManyClause clause = new ManyClause(pop("OR"), ManyClause.OR_OPERATION);
		push(clause);
		addNeedsFuture(clause);
		return this;
	}

	/**
	 * OR operation which takes 2 arguments and OR's them together.
	 *
	 * <p>
	 * <b>NOTE:</b> There is no guarantee of the order of the clauses that are generated in the final query.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> I can't remove the generics code warning that can be associated with this method. You can instead
	 * use the {@link #or(int)} method.
	 * </p>
	 */
	public Where<T, ID> or(Where<T, ID> left, Where<T, ID> right, Where<T, ID>... others) {
		Clause[] clauses = buildClauseArray(others, "OR");
		Clause secondClause = pop("OR");
		Clause firstClause = pop("OR");
		addClause(new ManyClause(firstClause, secondClause, clauses, ManyClause.OR_OPERATION));
		return this;
	}

	/**
	 * This method needs to be used carefully. This will absorb a number of clauses that were registered previously with
	 * calls to {@link Where#eq(String, Object)} or other methods and will string them together with OR's. There is no
	 * way to verify the number of previous clauses so the programmer has to count precisely.
	 *
	 * <p>
	 * <b>NOTE:</b> There is no guarantee of the order of the clauses that are generated in the final query.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b> This will throw an exception if numClauses is 0 but will work with 1 or more.
	 * </p>
	 */
	public Where<T, ID> or(int numClauses) {
		if (numClauses == 0) {
			throw new IllegalArgumentException("Must have at least one clause in or(numClauses)");
		}
		Clause[] clauses = new Clause[numClauses];
		for (int i = numClauses - 1; i >= 0; i--) {
			clauses[i] = pop("OR");
		}
		addClause(new ManyClause(clauses, ManyClause.OR_OPERATION));
		return this;
	}

	/**
	 * Add a clause where the ID is equal to the argument.
	 */
	public Where<T, ID> idEq(ID id) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		addClause(new SimpleComparison(openHelper, idColumnName, idFieldType, id, SimpleComparison.EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Add a clause where the ID is from an existing object.
	 */
	public <OD> Where<T, ID> idEq(Dao<OD, ?> dataDao, OD data) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		addClause(new SimpleComparison(openHelper, idColumnName, idFieldType, dataDao.extractId(data),
				SimpleComparison.EQUAL_TO_OPERATION));
		return this;
	}

	/**
	 * Add a raw statement as part of the where that can be anything that the database supports. Using more structured
	 * methods is recommended but this gives more control over the query and allows you to utilize database specific
	 * features.
	 *
	 * @param rawStatement
	 *            The statement that we should insert into the WHERE.
	 *
	 * @param args
	 *            Optional arguments that correspond to any ? specified in the rawStatement. Each of the arguments must
	 *            have either the corresponding columnName or the sql-type set. <b>WARNING,</b> you cannot use the
	 *            {@code SelectArg("columnName")} constructor since that sets the _value_, not the name. Use
	 *            {@code new SelectArg("column-name", null);}.
	 */
	public Where<T, ID> raw(String rawStatement, ArgumentHolder... args) throws SQLException
	{
		for (ArgumentHolder arg : args) {
			String columnName = arg.getColumnName();
			if (columnName == null) {
				if (arg.getSqlType() == null) {
					throw new IllegalArgumentException("Either the column name or SqlType must be set on each argument");
				}
			} else {
				arg.setMetaInfo(findColumnFieldType(columnName));
			}
		}
		addClause(new Raw(rawStatement, args));
		return this;
	}

	/**
	 * Make a comparison where the operator is specified by the caller. It is up to the caller to specify an appropriate
	 * operator for the database and that it be formatted correctly.
	 */
	public Where<T, ID> rawComparison(String columnName, String rawOperator, Object value) throws SQLException {
		addClause(new SimpleComparison(openHelper, columnName, findColumnFieldType(columnName), value, rawOperator));
		return this;
	}

	/**
	 * @deprecated Should now use {@link #reset()}.
	 */
	@Deprecated
	public Where<T, ID> clear() {
		return reset();
	}

	/**
	 * Reset the Where object so it can be re-used.
	 */
	public Where<T, ID> reset() {
		for (int i = 0; i < clauseStackLevel; i++) {
			// help with gc
			clauseStack[i] = null;
		}
		clauseStackLevel = 0;
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
		if (clauseStackLevel == 0) {
			throw new IllegalStateException("No where clauses defined.  Did you miss a where operation?");
		}
		if (clauseStackLevel != 1) {
			throw new IllegalStateException(
					"Both the \"left-hand\" and \"right-hand\" clauses have been defined.  Did you miss an AND or OR?");
		}
		if (needsFuture != null) {
			throw new IllegalStateException(
					"The SQL statement has not been finished since there are previous operations still waiting for clauses.");
		}

		// we don't pop here because we may want to run the query multiple times
		peek().appendSql(tableName, sb, columnArgList);
	}

	@Override
	public String toString() {
		if (clauseStackLevel == 0) {
			return "empty where clause";
		} else {
			Clause clause = peek();
			return "where clause: " + clause;
		}
	}

	private Where<T, ID> in(boolean in, String columnName, Object... objects) throws SQLException {
		if (objects.length == 1) {
			if (objects[0].getClass().isArray()) {
				throw new IllegalArgumentException("Object argument to " + (in ? "IN" : "notId")
						+ " seems to be an array within an array");
			}
			if (objects[0] instanceof Where) {
				throw new IllegalArgumentException("Object argument to " + (in ? "IN" : "notId")
						+ " seems to be a Where object, did you mean the QueryBuilder?");
			}
		}
		addClause(new In(openHelper, columnName, findColumnFieldType(columnName), objects, in));
		return this;
	}



	private Clause[] buildClauseArray(Where<T, ID>[] others, String label) {
		Clause[] clauses;
		if (others.length == 0) {
			clauses = null;
		} else {
			clauses = new Clause[others.length];
			// fill in reverse order
			for (int i = others.length - 1; i >= 0; i--) {
				clauses[i] = pop(label);
			}
		}
		return clauses;
	}

	private void addNeedsFuture(NeedsFutureClause clause) {
		if (needsFuture != null) {
			throw new IllegalStateException(needsFuture + " is already waiting for a future clause, can't add: "
					+ clause);
		}
		needsFuture = clause;
	}

	private void addClause(Clause clause) {
		if (needsFuture == null) {
			push(clause);
		} else {
			// we have a binary statement which was called before the right clause was defined
			needsFuture.setMissingClause(clause);
			needsFuture = null;
		}
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

	private void push(Clause clause) {
		// if the stack is full then we need to grow it
		if (clauseStackLevel == clauseStack.length) {
			// double its size each time
			Clause[] newStack = new Clause[clauseStackLevel * 2];
			// copy the entries over to the new stack
			for (int i = 0; i < clauseStackLevel; i++) {
				newStack[i] = clauseStack[i];
				// to help gc
				clauseStack[i] = null;
			}
			clauseStack = newStack;
		}
		clauseStack[clauseStackLevel++] = clause;
	}

	private Clause pop(String label) {
		if (clauseStackLevel == 0) {
			throw new IllegalStateException("Expecting there to be a clause already defined for '" + label
					+ "' operation");
		}
		Clause clause = clauseStack[--clauseStackLevel];
		// to help gc
		clauseStack[clauseStackLevel] = null;
		return clause;
	}

	private Clause peek() {
		return clauseStack[clauseStackLevel - 1];
	}

}
