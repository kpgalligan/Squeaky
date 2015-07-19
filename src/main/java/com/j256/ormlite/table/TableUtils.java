package com.j256.ormlite.table;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.squeaky.SqueakyOpenHelper;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Couple utility methods for the creating, dropping, and maintenance of tables.
 * 
 * @author graywatson
 */
public class TableUtils {

	private static Logger logger = LoggerFactory.getLogger(TableUtils.class);

	public static final AndroidDatabaseType databaseType = new AndroidDatabaseType();

	/**
	 * For static methods only.
	 */
	private TableUtils() {
	}

	/**
	 * Issue the database statements to create the table associated with a table configuration.
	 * 
	 * @param connectionSource
	 *            connectionSource Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int createTables(SQLiteDatabase connectionSource, Class... clazz)
			throws SQLException {
		int count = 0;
		for (Class aClass : clazz)
		{
			count += createTable(connectionSource, aClass, false);
		}
		return count;
	}

	private static  GeneratedTableMapper loadTableMapper(Class dataclass)
	{
		return SqueakyOpenHelper.loadGeneratedTableMapper(dataclass);
	}

	/**
	 * Create a table if it does not already exist. This is not supported by all databases.
	 */
	public static <T, ID> int createTablesIfNotExists(SQLiteDatabase connectionSource, Class... clazz)
			throws SQLException {
		int count = 0;
		for (Class aClass : clazz)
		{
			count += createTable(connectionSource, aClass, true);
		}
		return count;
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param connectionSource
	 *            Our connect source which is used to get the database type, not to apply the creates.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The collection of table create statements.
	 */
	public static <T, ID> List<String> getCreateTableStatements(SQLiteDatabase connectionSource,
			Class clazz) throws SQLException {

			return addCreateTableStatements(loadTableMapper(clazz), false);
	}

	/**
	 * Issue the database statements to drop the table associated with a table configuration.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 * 
	 * @param connectionSource
	 *            Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int dropTables(SQLiteDatabase connectionSource, boolean ignoreErrors, Class... clazz) throws SQLException
	{
		int count = 0;
		for (Class aClass : clazz)
		{
			count += doDropTable(connectionSource, loadTableMapper(aClass), ignoreErrors);
		}
		return count;
	}

	/**
	 * Clear all data out of the table. For certain database types and with large sized tables, which may take a long
	 * time. In some configurations, it may be faster to drop and re-create the table.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 */
	public static <T, ID> void clearTable(SQLiteDatabase connectionSource, Class clazz)
			throws SQLException {
		clearTable(connectionSource, loadTableMapper(clazz).getTableConfig().getTableName());
	}

	private static <T, ID> int createTable(SQLiteDatabase connectionSource, Class clazz, boolean ifNotExists) throws SQLException {

		return doCreateTable(connectionSource, loadTableMapper(clazz), ifNotExists);
	}

	private static void clearTable(SQLiteDatabase connectionSource, String tableName) throws SQLException {
		StringBuilder sb = new StringBuilder(48);

		sb.append("DELETE FROM ");

		databaseType.appendEscapedEntityName(sb, tableName);
		String statement = sb.toString();
		logger.info("clearing table '{}' with '{}", tableName, statement);
		connectionSource.execSQL(sb.toString());
	}

	private static <T, ID> int doDropTable(SQLiteDatabase connectionSource,
			GeneratedTableMapper<T, ID> tableInfo, boolean ignoreErrors) throws SQLException {
		logger.info("dropping table '{}'", tableInfo.getTableConfig().getTableName());
		List<String> statements = new ArrayList<String>();
		addDropIndexStatements(tableInfo, statements);
		addDropTableStatements(tableInfo, statements);

		return doStatements(connectionSource, "drop", statements, ignoreErrors,
				false, false);

	}

	private static <T, ID> void addDropIndexStatements(GeneratedTableMapper<T, ID> tableInfo,
			List<String> statements) throws SQLException
	{
		// run through and look for index annotations
		Set<String> indexSet = new HashSet<String>();
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes()) {
			String indexName = fieldType.getIndexName();
			if (indexName != null) {
				indexSet.add(indexName);
			}
			String uniqueIndexName = fieldType.getUniqueIndexName();
			if (uniqueIndexName != null) {
				indexSet.add(uniqueIndexName);
			}
		}

		StringBuilder sb = new StringBuilder(48);
		for (String indexName : indexSet) {
			logger.info("dropping index '{}' for table '{}", indexName, tableInfo.getTableConfig().getTableName());
			sb.append("DROP INDEX ");
			databaseType.appendEscapedEntityName(sb, indexName);
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to create a database table and any associated features.
	 */
	private static <T, ID> void addCreateTableStatements(GeneratedTableMapper<T, ID> tableInfo,
			List<String> statements, boolean ifNotExists) throws SQLException {
		StringBuilder sb = new StringBuilder(256);
		sb.append("CREATE TABLE ");
		if (ifNotExists) {
			sb.append("IF NOT EXISTS ");
		}
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
		sb.append(" (");
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		// our statement will be set here later
		boolean first = true;
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}

				// we have to call back to the database type for the specific create syntax
			databaseType.appendColumnArg(sb, fieldType, additionalArgs);
		}
		// add any sql that sets any primary key fields
		databaseType.addPrimaryKeySql(tableInfo.getTableConfig().getFieldTypes(), additionalArgs);
		// add any sql that sets any unique fields
		databaseType.addUniqueComboSql(tableInfo.getTableConfig().getFieldTypes(), additionalArgs);
		for (String arg : additionalArgs) {
			// we will have spat out one argument already so we don't have to do the first dance
			sb.append(", ").append(arg);
		}
		sb.append(") ");

		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
		addCreateIndexStatements(tableInfo, statements, ifNotExists, false);
		addCreateIndexStatements(tableInfo, statements, ifNotExists, true);
	}

	private static <T, ID> void addCreateIndexStatements(GeneratedTableMapper<T, ID> tableInfo,
			List<String> statements, boolean ifNotExists, boolean unique) throws SQLException
	{
		// run through and look for index annotations
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes()) {
			String indexName;
			if (unique) {
				indexName = fieldType.getUniqueIndexName();
			} else {
				indexName = fieldType.getIndexName();
			}
			if (indexName == null) {
				continue;
			}

			List<String> columnList = indexMap.get(indexName);
			if (columnList == null) {
				columnList = new ArrayList<String>();
				indexMap.put(indexName, columnList);
			}
			columnList.add(fieldType.getColumnName());
		}

		StringBuilder sb = new StringBuilder(128);
		for (Map.Entry<String, List<String>> indexEntry : indexMap.entrySet()) {
			logger.info("creating index '{}' for table '{}", indexEntry.getKey(), tableInfo.getTableConfig().getTableName());
			sb.append("CREATE ");
			if (unique) {
				sb.append("UNIQUE ");
			}
			sb.append("INDEX ");
			if (ifNotExists) {
				sb.append("IF NOT EXISTS ");
			}
			databaseType.appendEscapedEntityName(sb, indexEntry.getKey());
			sb.append(" ON ");
			databaseType.appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
			sb.append(" ( ");
			boolean first = true;
			for (String columnName : indexEntry.getValue()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				databaseType.appendEscapedEntityName(sb, columnName);
			}
			sb.append(" )");
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to drop a database table.
	 */
	private static <T, ID> void addDropTableStatements(GeneratedTableMapper<T, ID> tableInfo,
			List<String> statements) throws SQLException
	{
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();

		StringBuilder sb = new StringBuilder(64);
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
		sb.append(' ');
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
	}

	private static <T, ID> int doCreateTable(SQLiteDatabase connectionSource, GeneratedTableMapper<T, ID> tableInfo,
			boolean ifNotExists) throws SQLException {
		logger.info("creating table '{}'", tableInfo.getTableConfig().getTableName());
		List<String> statements = new ArrayList<String>();
		addCreateTableStatements(tableInfo, statements, ifNotExists);

			int stmtC =
					doStatements(connectionSource, "create", statements, false, false, false);
			return stmtC;

	}

	private static int doStatements(SQLiteDatabase connection, String label, Collection<String> statements,
			boolean ignoreErrors, boolean returnsNegative, boolean expectingZero) throws SQLException {
		int stmtC = 0;
		for (String statement : statements) {

			try {
				connection.execSQL(statement);
				logger.info("executed {} table statement changed: {}", label, statement);
			} catch (Exception e) {
				if (ignoreErrors) {
					logger.info("ignoring {} error '{}' for statement: {}", label, e, statement);
				} else {
					throw SqlExceptionUtil.create("SQL statement failed: " + statement, e);
				}
			}

			stmtC++;
		}
		return stmtC;
	}

	private static <T, ID> List<String> addCreateTableStatements(GeneratedTableMapper<T, ID> tableInfo, boolean ifNotExists) throws SQLException {
		List<String> statements = new ArrayList<String>();
		addCreateTableStatements(tableInfo, statements, ifNotExists);
		return statements;
	}
}
