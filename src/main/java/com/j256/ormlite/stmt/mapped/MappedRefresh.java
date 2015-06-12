package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.GeneratedTableMapper;
import com.j256.ormlite.table.TableInfo;

import java.sql.SQLException;

/**
 * Created by kgalligan on 6/7/15.
 */
public class MappedRefresh<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedRefresh(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes) {
		super(tableInfo, statement, argFieldTypes);
	}

	/**
	 * Execute our refresh query statement and then update all of the fields in data with the fields from the result.
	 *
	 * @return 1 if we found the object in the table by id or 0 if not.
	 */
	public void executeRefresh(DatabaseConnection databaseConnection, T data, ObjectCache objectCache)
			throws SQLException
	{
		GeneratedTableMapper<T, ID> generatedTableMapper = tableInfo.getGeneratedTableMapper();

		@SuppressWarnings("unchecked")
		ID id = (ID) generatedTableMapper.extractId(data);

		Object[] args = new Object[]{convertIdToFieldObject(id)};
		databaseConnection.queryForOneRefresh(statement, args, argFieldTypes, generatedTableMapper, data, objectCache);
	}

	/*public static <T, ID> MappedRefresh<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot refresh " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		String statement = buildStatement(databaseType, tableInfo, idField);
		return new MappedRefresh<T, ID>(tableInfo, statement, new FieldType[] { tableInfo.getIdField() },
				tableInfo.getFieldTypes());
	}*/

	protected static <T, ID> String buildStatement(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) throws SQLException
	{
		String selectStatement = new QueryBuilder<T, ID>(databaseType, tableInfo, dao).prepare().getStatement();

		// build the select statement by hand
		StringBuilder sb = new StringBuilder(selectStatement.length() + 20);
		sb.append(selectStatement);

		appendWhereFieldEq(databaseType, tableInfo.getIdField(), sb, null);
		return sb.toString();
	}

	public static <T, ID> MappedRefresh<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot refresh " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		String statement = buildStatement(databaseType, tableInfo, dao);
		return new MappedRefresh<T, ID>(tableInfo, statement, tableInfo.getFieldTypes());
	}
}
