package com.j256.ormlite.stmt;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.mapped.*;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Executes SQL statements for a particular table in a particular database. Basically a call through to various mapped
 * statement methods.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class StatementExecutor<T, ID>
{

	private static Logger logger = LoggerFactory.getLogger(StatementExecutor.class);
	private static final FieldType[] noFieldTypes = new FieldType[0];

	private final DatabaseType databaseType;
	private final TableInfo<T, ID> tableInfo;
	private final Dao<T, ID> dao;
	private MappedQueryForId<T, ID> mappedQueryForId;
	private PreparedQuery<T> preparedQueryForAll;
	private MappedCreate<T, ID> mappedInsert;
	private MappedUpdate<T, ID> mappedUpdate;
	private MappedUpdateId<T, ID> mappedUpdateId;
	private MappedDelete<T, ID> mappedDelete;
	private MappedRefresh<T, ID> mappedRefresh;
	private String countStarQuery;
	private String ifExistsQuery;
	private FieldType[] ifExistsFieldTypes;

	private final ThreadLocal<Boolean> localIsInBatchMode = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * Provides statements for various SQL operations.
	 */
	public StatementExecutor(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.dao = dao;
	}

	/**
	 * Return the object associated with the id or null if none. This does a SQL
	 * {@code SELECT col1,col2,... FROM ... WHERE ... = id} type query.
	 */
	public T queryForId(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (mappedQueryForId == null) {
			mappedQueryForId = MappedQueryForId.build(databaseType, tableInfo, dao, null);
		}
		return mappedQueryForId.execute(databaseConnection, id, objectCache);
	}

	/**
	 * Return the first object that matches the {@link PreparedStmt} or null if none.
	 */
	public T queryForFirst(DatabaseConnection databaseConnection, PreparedStmt<T> preparedStmt, ObjectCache objectCache)
			throws SQLException {
		CompiledStatement compiledStatement = preparedStmt.compile(databaseConnection, StatementType.SELECT);
		DatabaseResults results = null;
		try {
			results = compiledStatement.runQuery(objectCache);
			if (results.first()) {
				logger.debug("query-for-first of '{}' returned at least 1 result", preparedStmt.getStatement());
				return preparedStmt.mapRow(results);
			} else {
				logger.debug("query-for-first of '{}' returned at 0 results", preparedStmt.getStatement());
				return null;
			}
		} finally {
			IOUtils.closeThrowSqlException(results, "results");
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
		}
	}

	/**
	 * Return a list of all of the data in the table. Should be used carefully if the table is large. Consider using the
	 * {@link Dao#iterator} if this is the case.
	 */
	public List<T> queryForAll(ConnectionSource connectionSource, ObjectCache objectCache) throws SQLException {
		prepareQueryForAll();
		return query(connectionSource, preparedQueryForAll, objectCache);
	}

	/**
	 * Return a long value which is the number of rows in the table.
	 */
	public long queryForCountStar(DatabaseConnection databaseConnection) throws SQLException {
		if (countStarQuery == null) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("SELECT COUNT(*) FROM ");
			databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
			countStarQuery = sb.toString();
		}
		long count = databaseConnection.queryForLong(countStarQuery);
		logger.debug("query of '{}' returned {}", countStarQuery, count);
		return count;
	}

	/**
	 * Return a long value from a prepared query.
	 */
	public long queryForLong(DatabaseConnection databaseConnection, PreparedStmt<T> preparedStmt) throws SQLException {
		CompiledStatement compiledStatement = preparedStmt.compile(databaseConnection, StatementType.SELECT_LONG);
		DatabaseResults results = null;
		try {
			results = compiledStatement.runQuery(null);
			if (results.first()) {
				return results.getLong(0);
			} else {
				throw new SQLException("No result found in queryForLong: " + preparedStmt.getStatement());
			}
		} finally {
			IOUtils.closeThrowSqlException(results, "results");
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
		}
	}

	/**
	 * Return a long from a raw query with String[] arguments.
	 */
	public long queryForLong(DatabaseConnection databaseConnection, String query, String[] arguments)
			throws SQLException {
		logger.debug("executing raw query for long: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		CompiledStatement compiledStatement = null;
		DatabaseResults results = null;
		try {
			compiledStatement =
					databaseConnection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(compiledStatement, arguments);
			results = compiledStatement.runQuery(null);
			if (results.first()) {
				return results.getLong(0);
			} else {
				throw new SQLException("No result found in queryForLong: " + query);
			}
		} finally {
			IOUtils.closeThrowSqlException(results, "results");
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
		}
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedStmt}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public List<T> query(ConnectionSource connectionSource, PreparedStmt<T> preparedStmt, ObjectCache objectCache)
			throws SQLException {
		SelectIterator<T, ID> iterator =
				buildIterator(/* no dao specified because no removes */null, connectionSource, preparedStmt, objectCache,
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
		try {
			List<T> results = new ArrayList<T>();
			while (iterator.hasNextThrow()) {
				results.add(iterator.nextThrow());
			}
			logger.debug("query of '{}' returned {} results", preparedStmt.getStatement(), results.size());
			return results;
		} finally {
			IOUtils.closeThrowSqlException(iterator, "iterator");
		}
	}

	/**
	 * Create and return a SelectIterator for the class using the default mapped query for all statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource,
			int resultFlags, ObjectCache objectCache) throws SQLException {
		prepareQueryForAll();
		return buildIterator(classDao, connectionSource, preparedQueryForAll, objectCache, resultFlags);
	}

	/**
	 * Return a row mapper suitable for mapping 'select *' queries.
	 */
	public GenericRowMapper<T> getSelectStarRowMapper() throws SQLException {
		prepareQueryForAll();
		return preparedQueryForAll;
	}

	/**
	 * Create and return an {@link SelectIterator} for the class using a prepared statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource,
			PreparedStmt<T> preparedStmt, ObjectCache objectCache, int resultFlags) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = preparedStmt.compile(connection, StatementType.SELECT, resultFlags);
			SelectIterator<T, ID> iterator =
					new SelectIterator<T, ID>(tableInfo.getDataClass(), classDao, preparedStmt, connectionSource,
							connection, compiledStatement, preparedStmt.getStatement(), objectCache);
			connection = null;
			compiledStatement = null;
			return iterator;
		} finally {
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Create a new entry in the database from an object.
	 */
	public int create(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedInsert == null) {
			mappedInsert = MappedCreate.build(databaseType, tableInfo);
		}
		int result = mappedInsert.insert(databaseType, databaseConnection, data, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Update an object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedUpdate == null) {
			mappedUpdate = MappedUpdate.build(databaseType, tableInfo);
		}
		int result = mappedUpdate.update(databaseConnection, data, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Update an object in the database to change its id to the newId parameter.
	 */
	public int updateId(DatabaseConnection databaseConnection, T data, ID newId, ObjectCache objectCache)
			throws SQLException {
		if (mappedUpdateId == null) {
			mappedUpdateId = MappedUpdateId.build(databaseType, tableInfo);
		}
		int result = mappedUpdateId.execute(databaseConnection, data, newId, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Update rows in the database.
	 */
	public int update(DatabaseConnection databaseConnection, PreparedUpdate<T> preparedUpdate) throws SQLException {
		CompiledStatement compiledStatement = preparedUpdate.compile(databaseConnection, StatementType.UPDATE);
		try {
			int result = compiledStatement.runUpdate();
			if (dao != null && !localIsInBatchMode.get()) {
				dao.notifyChanges();
			}
			return result;
		} finally {
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
		}
	}

	/**
	 * Does a query for the object's Id and copies in each of the field values from the database to refresh the data
	 * parameter.
	 */
	public void refresh(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedRefresh == null) {
			mappedRefresh = MappedRefresh.build(databaseType, tableInfo, dao);
		}
		mappedRefresh.executeRefresh(databaseConnection, data, objectCache);
	}

	/**
	 * Delete an object from the database.
	 */
	public int delete(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedDelete == null) {
			mappedDelete = MappedDelete.build(databaseType, tableInfo);
		}
		int result = mappedDelete.delete(databaseConnection, data, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Delete an object from the database by id.
	 */
	public int deleteById(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (mappedDelete == null) {
			mappedDelete = MappedDelete.build(databaseType, tableInfo);
		}
		int result = mappedDelete.deleteById(databaseConnection, id, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteObjects(DatabaseConnection databaseConnection, Collection<T> datas, ObjectCache objectCache)
			throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		int result =
				MappedDeleteCollection.deleteObjects(databaseType, tableInfo, databaseConnection, datas, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteIds(DatabaseConnection databaseConnection, Collection<ID> ids, ObjectCache objectCache)
			throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		int result = MappedDeleteCollection.deleteIds(databaseType, tableInfo, databaseConnection, ids, objectCache);
		if (dao != null && !localIsInBatchMode.get()) {
			dao.notifyChanges();
		}
		return result;
	}

	/**
	 * Delete rows that match the prepared statement.
	 */
	public int delete(DatabaseConnection databaseConnection, PreparedDelete<T> preparedDelete) throws SQLException {
		CompiledStatement compiledStatement = preparedDelete.compile(databaseConnection, StatementType.DELETE);
		try {
			int result = compiledStatement.runUpdate();
			if (dao != null && !localIsInBatchMode.get()) {
				dao.notifyChanges();
			}
			return result;
		} finally {
			IOUtils.closeThrowSqlException(compiledStatement, "compiled statement");
		}
	}

	public boolean ifExists(DatabaseConnection connection, ID id) throws SQLException {
		throw new UnsupportedOperationException("need single value query");
		/*if (ifExistsQuery == null) {
			QueryBuilder<T, ID> qb = new QueryBuilder<T, ID>(databaseType, tableInfo, dao);
			qb.selectRaw("COUNT(*)");
			*//*
			 * NOTE: bit of a hack here because the select arg is never used but it _can't_ be a constant because we set
			 * field-name and field-type on it.
			 *//*
			qb.where().eq(tableInfo.getIdField().getColumnName(), new SelectArg());
			ifExistsQuery = qb.prepareStatementString();
			ifExistsFieldTypes = new FieldType[] { tableInfo.getIdField() };
		}
		Object idSqlArg = tableInfo.getIdField().convertJavaFieldToSqlArgValue(id);
		long count = connection.queryForLong(ifExistsQuery, new Object[] { idSqlArg }, ifExistsFieldTypes);
		logger.debug("query of '{}' returned {}", ifExistsQuery, count);
		return (count != 0);*/
	}

	private void assignStatementArguments(CompiledStatement compiledStatement, String[] arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++) {
			compiledStatement.setObject(i, arguments[i], SqlType.STRING);
		}
	}

	private void prepareQueryForAll() throws SQLException {
		if (preparedQueryForAll == null) {
			preparedQueryForAll = new QueryBuilder<T, ID>(databaseType, tableInfo, dao).prepare();
		}
	}
}
