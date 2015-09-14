package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.table.GeneratedTableMapper;

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

	public Clause eq(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.EQUAL_TO_OPERATION);
	}

	public Clause gt(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.GREATER_THAN_OPERATION);
	}

	public Clause ge(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.GREATER_THAN_EQUAL_TO_OPERATION);
	}

	public Clause lt(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LESS_THAN_OPERATION);
	}

	public Clause le(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LESS_THAN_EQUAL_TO_OPERATION);
	}

	public Clause like(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.LIKE_OPERATION);
	}

	public Clause ne(Class c, String columnFieldName, Object value) throws SQLException
	{
		return initOp(c, columnFieldName, value, SimpleComparison.NOT_EQUAL_TO_OPERATION);
	}

	public Clause in(Class c, String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);
		return new In(fieldType, objects, true);
	}

	public Clause notIn(Class c, String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);
		return new In(fieldType, objects, false);
	}

	public Clause in(Class c, String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);
		return new In(fieldType, objects, true);
	}

	public Clause notIn(Class c, String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);
		return new In(fieldType, objects, false);
	}

	public Clause between(Class c, String columnFieldName, Object low, Object high) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);
		return new Between(fieldType, low, high);
	}

	public Clause isNull(Class c, String columnFieldName) throws SQLException
	{
		return new IsNull(squeakyContext.findFieldType(c, columnFieldName));
	}

	public Clause isNotNull(Class c, String columnFieldName) throws SQLException
	{
		return new IsNotNull(squeakyContext.findFieldType(c, columnFieldName));
	}

	private Clause initOp(Class c, String columnFieldName, Object value, String op) throws SQLException
	{
		FieldType fieldType = squeakyContext.findFieldType(c, columnFieldName);

		return new SimpleComparison(fieldType, value, op);
	}
}
