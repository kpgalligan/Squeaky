package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.JoinAlias;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by kgalligan on 9/8/15.
 */
public class QueryFactory
{
	private final SqueakyContext squeakyContext;

	public QueryFactory(SqueakyContext squeakyContext, Class c)
	{
		this.squeakyContext = squeakyContext;
	}

	public Clause eq(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.EQUAL_TO_OPERATION);
	}

	public Clause gt(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.GREATER_THAN_OPERATION);
	}

	public Clause ge(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.GREATER_THAN_EQUAL_TO_OPERATION);
	}

	public Clause lt(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LESS_THAN_OPERATION);
	}

	public Clause le(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LESS_THAN_EQUAL_TO_OPERATION);
	}

	public Clause like(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LIKE_OPERATION);
	}

	public Clause ne(JoinAlias c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.NOT_EQUAL_TO_OPERATION);
	}

	public Clause in(JoinAlias c, String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c.tableType, columnFieldName);
		return new In(fieldType, objects, true, c);
	}

	public Clause notIn(JoinAlias c, String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c.tableType, columnFieldName);
		return new In(fieldType, objects, false, c);
	}

	public Clause in(JoinAlias c, String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c.tableType, columnFieldName);
		return new In(fieldType, objects, true, c);
	}

	public Clause notIn(JoinAlias c, String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c.tableType, columnFieldName);
		return new In(fieldType, objects, false, c);
	}

	public Clause between(JoinAlias c, String columnFieldName, Object low, Object high) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c.tableType, columnFieldName);
		return new Between(fieldType, low, high, c);
	}

	public Clause isNull(JoinAlias c, String columnFieldName) throws SQLException
	{
		return new IsNull(squeakyContext.findFieldType(c.tableType, columnFieldName), c);
	}

	public Clause isNotNull(JoinAlias c, String columnFieldName) throws SQLException
	{
		return new IsNotNull(squeakyContext.findFieldType(c.tableType, columnFieldName), c);
	}

	private Clause initOp(JoinAlias joinAlias, String columnFieldName, Object value, String op) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(joinAlias.tableType, columnFieldName);

		return new SimpleComparison(fieldType, value, op, joinAlias);
	}
}
