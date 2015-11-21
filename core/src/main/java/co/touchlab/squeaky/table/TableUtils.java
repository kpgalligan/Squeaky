package co.touchlab.squeaky.table;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.db.SQLiteDatabase;
import co.touchlab.squeaky.db.sqlite.SqueakyOpenHelper;
import co.touchlab.squeaky.field.DataPersister;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.logger.OLog;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Couple utility methods for the creating, dropping, and maintenance of tables.
 *
 * @author graywatson
 */
public class TableUtils
{

	public static final String TAG = TableUtils.class.getSimpleName();

	/**
	 * For static methods only.
	 */
	private TableUtils()
	{
	}

	public static <T> int createTables(SQLiteDatabase connectionSource, Class... clazz)
			throws SQLException
	{
		int count = 0;
		for (Class aClass : clazz)
		{
			count += createTable(connectionSource, aClass, false);
		}
		return count;
	}

	private static GeneratedTableMapper loadTableMapper(Class dataclass)
	{
		return SqueakyContext.loadGeneratedTableMapper(dataclass);
	}

	/**
	 * Create a table if it does not already exist. This is not supported by all databases.
	 */
	public static <T> int createTablesIfNotExists(SQLiteDatabase connectionSource, Class... clazz)
			throws SQLException
	{
		int count = 0;
		for (Class aClass : clazz)
		{
			count += createTable(connectionSource, aClass, true);
		}
		return count;
	}

	public static <T> List<String> getCreateTableStatements(SQLiteDatabase connectionSource,
																Class clazz) throws SQLException
	{

		return addCreateTableStatements(loadTableMapper(clazz), false);
	}

	public static <T> int dropTables(SQLiteDatabase connectionSource, boolean ignoreErrors, Class... clazz) throws SQLException
	{
		int count = 0;
		for (Class aClass : clazz)
		{
			count += doDropTable(connectionSource, loadTableMapper(aClass), ignoreErrors);
		}
		return count;
	}

	public static <T> int dropViews(SQLiteDatabase connectionSource, boolean ignoreErrors, Class... clazz) throws SQLException
	{
		int count = 0;
		for (Class aClass : clazz)
		{
			count += doDropView(connectionSource, loadTableMapper(aClass), ignoreErrors);
		}
		return count;
	}

	/**
	 * Clear all data out of the table. For certain database types and with large sized tables, which may take a long
	 * time. In some configurations, it may be faster to drop and re-create the table.
	 * <p/>
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 */
	public static <T> void clearTable(SQLiteDatabase connectionSource, Class clazz)
			throws SQLException
	{
		clearTable(connectionSource, loadTableMapper(clazz).getTableConfig().getTableName());
	}

	private static <T> int createTable(SQLiteDatabase connectionSource, Class clazz, boolean ifNotExists) throws SQLException
	{

		return doCreateTable(connectionSource, loadTableMapper(clazz), ifNotExists);
	}

	private static void clearTable(SQLiteDatabase connectionSource, String tableName) throws SQLException
	{
		StringBuilder sb = new StringBuilder(48);

		sb.append("DELETE FROM ");

		appendEscapedEntityName(sb, tableName);
		String statement = sb.toString();
		OLog.i(TAG, "clearing table '{" + tableName + "}' with '{" + statement + "}");
		connectionSource.execSQL(sb.toString());
	}

	private static <T> int doDropTable(SQLiteDatabase connectionSource,
										   GeneratedTableMapper<T> tableInfo, boolean ignoreErrors) throws SQLException
	{
		OLog.i(TAG, "dropping table '{" + tableInfo.getTableConfig().getTableName() + "}'");
		List<String> statements = new ArrayList<String>();
		addDropIndexStatements(tableInfo, statements);
		addDropTableStatements(tableInfo, statements);

		return doStatements(connectionSource, "drop", statements, ignoreErrors,
				false, false);

	}

	private static <T> int doDropView(SQLiteDatabase connectionSource,
										  GeneratedTableMapper<T> tableInfo, boolean ignoreErrors) throws SQLException
	{
		OLog.i(TAG, "dropping table '{" + tableInfo.getTableConfig().getTableName() + "}'");
		List<String> statements = new ArrayList<String>();

		addDropViewStatements(tableInfo, statements);

		return doStatements(connectionSource, "drop", statements, ignoreErrors,
				false, false);

	}

	private static <T> void addDropIndexStatements(GeneratedTableMapper<T> tableInfo,
													   List<String> statements) throws SQLException
	{
		// run through and look for index annotations
		Set<String> indexSet = new HashSet<String>();
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes())
		{
			String indexName = fieldType.getIndexName();
			if (indexName != null)
			{
				indexSet.add(indexName);
			}
			String uniqueIndexName = fieldType.getUniqueIndexName();
			if (uniqueIndexName != null)
			{
				indexSet.add(uniqueIndexName);
			}
		}

		StringBuilder sb = new StringBuilder(48);
		for (String indexName : indexSet)
		{
			OLog.i(TAG, "dropping index '{" + indexName + "}' for table '{" + tableInfo.getTableConfig().getTableName() + "}");
			sb.append("DROP INDEX ");
			appendEscapedEntityName(sb, indexName);
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to create a database table and any associated features.
	 */
	private static <T> void addCreateTableStatements(GeneratedTableMapper<T> tableInfo,
														 List<String> statements, boolean ifNotExists) throws SQLException
	{
		StringBuilder sb = new StringBuilder(256);
		sb.append("CREATE TABLE ");
		if (ifNotExists)
		{
			sb.append("IF NOT EXISTS ");
		}
		appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
		sb.append(" (");
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		// our statement will be set here later
		boolean first = true;
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(", ");
			}

			// we have to call back to the database type for the specific create syntax
			appendColumnArg(sb, fieldType, additionalArgs);
		}
		// add any sql that sets any primary key fields
		addPrimaryKeySql(tableInfo.getTableConfig().getFieldTypes(), additionalArgs);
		// add any sql that sets any unique fields
		addUniqueComboSql(tableInfo.getTableConfig().getFieldTypes(), additionalArgs);
		for (String arg : additionalArgs)
		{
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

	private static <T> void addCreateIndexStatements(GeneratedTableMapper<T> tableInfo,
														 List<String> statements, boolean ifNotExists, boolean unique) throws SQLException
	{
		// run through and look for index annotations
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for (FieldType fieldType : tableInfo.getTableConfig().getFieldTypes())
		{
			String indexName;
			if (unique)
			{
				indexName = fieldType.getUniqueIndexName();
			}
			else
			{
				indexName = fieldType.getIndexName();
			}
			if (indexName == null)
			{
				continue;
			}

			List<String> columnList = indexMap.get(indexName);
			if (columnList == null)
			{
				columnList = new ArrayList<String>();
				indexMap.put(indexName, columnList);
			}
			columnList.add(fieldType.getColumnName());
		}

		StringBuilder sb = new StringBuilder(128);
		for (Map.Entry<String, List<String>> indexEntry : indexMap.entrySet())
		{
			OLog.i(TAG, "creating index '{" + indexEntry.getKey() + "}' for table '{" + tableInfo.getTableConfig().getTableName() + "}");
			sb.append("CREATE ");
			if (unique)
			{
				sb.append("UNIQUE ");
			}
			sb.append("INDEX ");
			if (ifNotExists)
			{
				sb.append("IF NOT EXISTS ");
			}
			appendEscapedEntityName(sb, indexEntry.getKey());
			sb.append(" ON ");
			appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
			sb.append(" ( ");
			boolean first = true;
			for (String columnName : indexEntry.getValue())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(", ");
				}
				appendEscapedEntityName(sb, columnName);
			}
			sb.append(" )");
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to drop a database table.
	 */
	private static <T> void addDropTableStatements(GeneratedTableMapper<T> tableInfo,
													   List<String> statements) throws SQLException
	{
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();

		StringBuilder sb = new StringBuilder(64);
		sb.append("DROP TABLE ");
		appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
		sb.append(' ');
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
	}

	private static <T> void addDropViewStatements(GeneratedTableMapper<T> tableInfo,
													  List<String> statements) throws SQLException
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append("DROP VIEW ");
		appendEscapedEntityName(sb, tableInfo.getTableConfig().getTableName());
		sb.append(' ');

		statements.add(sb.toString());
	}

	private static <T> int doCreateTable(SQLiteDatabase connectionSource, GeneratedTableMapper<T> tableInfo,
											 boolean ifNotExists) throws SQLException
	{
		OLog.i(TAG, "creating table '{" + tableInfo.getTableConfig().getTableName() + "}'");
		List<String> statements = new ArrayList<String>();
		addCreateTableStatements(tableInfo, statements, ifNotExists);

		int stmtC =
				doStatements(connectionSource, "create", statements, false, false, false);
		return stmtC;

	}

	private static int doStatements(SQLiteDatabase connection, String label, Collection<String> statements,
									boolean ignoreErrors, boolean returnsNegative, boolean expectingZero) throws SQLException
	{
		int stmtC = 0;
		for (String statement : statements)
		{

			try
			{
				connection.execSQL(statement);
				OLog.i(TAG, "executed {" + label + "} table statement changed: {" + statement + "}");
			}
			catch (Exception e)
			{
				if (ignoreErrors)
				{
					OLog.i(TAG, "ignoring {" + label + "} error for statement: {" + statement + "}", e);
				}
				else
				{
					throw SqlExceptionUtil.create("SQL statement failed: " + statement, e);
				}
			}

			stmtC++;
		}
		return stmtC;
	}

	private static <T> List<String> addCreateTableStatements(GeneratedTableMapper<T> tableInfo, boolean ifNotExists) throws SQLException
	{
		List<String> statements = new ArrayList<String>();
		addCreateTableStatements(tableInfo, statements, ifNotExists);
		return statements;
	}

	public static void appendEscapedEntityName(StringBuilder sb, String name)
	{
		sb.append('`').append(name).append('`');
	}

	/**
	 * Output the SQL type for the default value for the type.
	 */
	private static void appendDefaultValue(StringBuilder sb, FieldType fieldType, Object defaultValue)
	{
		if (fieldType.isEscapedDefaultValue())
		{
			appendEscapedWord(sb, defaultValue.toString());
		}
		else
		{
			sb.append(defaultValue);
		}
	}

	public static void addPrimaryKeySql(FieldType[] fieldTypes, List<String> additionalArgs)
	{
		StringBuilder sb = null;
		for (FieldType fieldType : fieldTypes)
		{
			if (fieldType.isGeneratedId())
			{
				// don't add anything
			}
			else if (fieldType.isId())
			{
				if (sb == null)
				{
					sb = new StringBuilder(48);
					sb.append("PRIMARY KEY (");
				}
				else
				{
					sb.append(',');
				}
				appendEscapedEntityName(sb, fieldType.getColumnName());
			}
		}
		if (sb != null)
		{
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	public static void addUniqueComboSql(FieldType[] fieldTypes, List<String> additionalArgs)
	{
		StringBuilder sb = null;
		for (FieldType fieldType : fieldTypes)
		{
			if (fieldType.isUniqueCombo())
			{
				if (sb == null)
				{
					sb = new StringBuilder(48);
					sb.append("UNIQUE (");
				}
				else
				{
					sb.append(',');
				}
				appendEscapedEntityName(sb, fieldType.getColumnName());
			}
		}
		if (sb != null)
		{
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	public static void appendEscapedWord(StringBuilder sb, String word)
	{
		sb.append('\'').append(word).append('\'');
	}

	/**
	 * Add SQL to handle a unique=true field. THis is not for uniqueCombo=true.
	 */
	private static void addSingleUnique(FieldType fieldType, List<String> additionalArgs)
	{
		StringBuilder alterSb = new StringBuilder();
		alterSb.append(" UNIQUE (");
		appendEscapedEntityName(alterSb, fieldType.getColumnName());
		alterSb.append(")");
		additionalArgs.add(alterSb.toString());
	}

	public static void appendColumnArg(StringBuilder sb, FieldType fieldType, List<String> additionalArgs) throws SQLException
	{
		appendEscapedEntityName(sb, fieldType.getColumnName());
		sb.append(' ');
		DataPersister dataPersister = fieldType.getDataPersister();
		// first try the per-field width

		switch (dataPersister.getSqlType())
		{

			case STRING:
				appendStringType(sb);
				break;

			case LONG_STRING:
				appendLongStringType(sb);
				break;

			case BOOLEAN:
				appendBooleanType(sb);
				break;

			case DATE:
				appendDateType(sb);
				break;

			case CHAR:
				appendCharType(sb);
				break;

			case BYTE:
				appendByteType(sb);
				break;

			case BYTE_ARRAY:
				appendByteArrayType(sb);
				break;

			case SHORT:
				appendShortType(sb);
				break;

			case INTEGER:
				appendIntegerType(sb);
				break;

			case LONG:
				appendLongType(sb, fieldType);
				break;

			case FLOAT:
				appendFloatType(sb);
				break;

			case DOUBLE:
				appendDoubleType(sb);
				break;

			case SERIALIZABLE:
				appendSerializableType(sb);
				break;

			case BIG_DECIMAL:
				appendBigDecimalNumericType(sb);
				break;

			case UNKNOWN:
			default:
				// shouldn't be able to get here unless we have a missing case
				throw new IllegalArgumentException("Unknown SQL-type " + dataPersister.getSqlType());
		}
		sb.append(' ');

		/*
		 * NOTE: the configure id methods must be in this order since isGeneratedIdSequence is also isGeneratedId and
		 * isId. isGeneratedId is also isId.
		 */
		if (fieldType.isGeneratedId())
		{
			configureGeneratedId(sb, fieldType);
		}
		// if we have a generated-id then neither the not-null nor the default make sense and cause syntax errors
		if (!fieldType.isGeneratedId())
		{
			Object defaultValue = fieldType.getDefaultValue();
			if (defaultValue != null)
			{
				sb.append("DEFAULT ");
				appendDefaultValue(sb, fieldType, defaultValue);
				sb.append(' ');
			}
			if (!fieldType.isCanBeNull())
			{
				sb.append("NOT NULL ");
			}
			if (fieldType.isUnique())
			{
				addSingleUnique(fieldType, additionalArgs);
			}
		}
	}

	/**
	 * Output the SQL type for a Java String.
	 */
	protected static void appendStringType(StringBuilder sb)
	{
		sb.append("VARCHAR");
	}

	/**
	 * Output the SQL type for a Java Long String.
	 */
	protected static void appendLongStringType(StringBuilder sb)
	{
		sb.append("TEXT");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected static void appendDateType(StringBuilder sb)
	{
		sb.append("TIMESTAMP");
	}

	/**
	 * Output the SQL type for a Java boolean.
	 */
	protected static void appendBooleanType(StringBuilder sb)
	{
		sb.append("BOOLEAN");
	}

	/**
	 * Output the SQL type for a Java char.
	 */
	protected static void appendCharType(StringBuilder sb)
	{
		sb.append("CHAR");
	}

	/**
	 * Output the SQL type for a Java byte.
	 */
	protected static void appendByteType(StringBuilder sb)
	{
		sb.append("TINYINT");
	}

	/**
	 * Output the SQL type for a Java short.
	 */
	protected static void appendShortType(StringBuilder sb)
	{
		sb.append("SMALLINT");
	}

	/**
	 * Output the SQL type for a Java integer.
	 */
	private static void appendIntegerType(StringBuilder sb)
	{
		sb.append("INTEGER");
	}

	/**
	 * Output the SQL type for a Java float.
	 */
	private static void appendFloatType(StringBuilder sb)
	{
		sb.append("FLOAT");
	}

	/**
	 * Output the SQL type for a Java double.
	 */
	private static void appendDoubleType(StringBuilder sb)
	{
		sb.append("DOUBLE PRECISION");
	}

	/**
	 * Output the SQL type for either a serialized Java object or a byte[].
	 */
	protected static void appendByteArrayType(StringBuilder sb)
	{
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a serialized Java object.
	 */
	protected static void appendSerializableType(StringBuilder sb)
	{
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a BigDecimal object.
	 */
	protected static void appendBigDecimalNumericType(StringBuilder sb)
	{
		sb.append("NUMERIC");
	}

	protected static void appendLongType(StringBuilder sb, FieldType fieldType)
	{
		/*
		 * This is unfortunate. SQLIte requires that a generated-id have the string "INTEGER PRIMARY KEY AUTOINCREMENT"
		 * even though the maximum generated value is 64-bit. See configureGeneratedId below.
		 */
		if (fieldType.getSqlType() == SqlType.LONG && fieldType.isGeneratedId())
		{
			sb.append("INTEGER");
		}
		else
		{
			sb.append("BIGINT");
		}
	}

	protected static void configureGeneratedId(StringBuilder sb, FieldType fieldType)
	{
		/*
		 * Even though the documentation talks about INTEGER, it is 64-bit with a maximum value of 9223372036854775807.
		 * See http://www.sqlite.org/faq.html#q1 and http://www.sqlite.org/autoinc.html
		 */
		if (fieldType.getSqlType() != SqlType.INTEGER && fieldType.getSqlType() != SqlType.LONG)
		{
			throw new IllegalArgumentException(
					"Sqlite requires that auto-increment generated-id be integer or long type");
		}
		sb.append("PRIMARY KEY AUTOINCREMENT ");
		// no additional call to configureId here
	}


}
