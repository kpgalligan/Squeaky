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
	private final Class c;

	public QueryFactory(SqueakyContext squeakyContext, Class c)
	{
		this.squeakyContext = squeakyContext;
		this.c = c;
	}

	public Clause eq(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.EQUAL_TO_OPERATION);
	}

	public Clause gt(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.GREATER_THAN_OPERATION);
	}

	public Clause ge(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.GREATER_THAN_EQUAL_TO_OPERATION);
	}

	public Clause lt(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.LESS_THAN_OPERATION);
	}

	public Clause le(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.LESS_THAN_EQUAL_TO_OPERATION);
	}

	public Clause like(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.LIKE_OPERATION);
	}

	public Clause ne(String columnFieldName, Object value) throws SQLException
	{
		return initOp(columnFieldName, value, SimpleComparison.NOT_EQUAL_TO_OPERATION);
	}

	public Clause in(String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new In(fieldType, objects, true);
	}

	public Clause notIn(String columnFieldName, Collection<?> objects) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new In(fieldType, objects, false);
	}

	public Clause in(String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new In(fieldType, objects, true);
	}

	public Clause notIn(String columnFieldName, Object... objects) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new In(fieldType, objects, false);
	}

	public Clause between(String columnFieldName, Object low, Object high) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new Between(fieldType, low, high);
	}

	public Clause isNull(String columnFieldName) throws SQLException
	{
		return new IsNull(findFieldType(columnFieldName));
	}

	public Clause isNotNull(String columnFieldName) throws SQLException
	{
		return new IsNotNull(findFieldType(columnFieldName));
	}

	private Clause initOp(String columnFieldName, Object value, String op) throws SQLException
	{
		FieldType fieldType = findFieldType(columnFieldName);
		return new SimpleComparison(fieldType, value, op);
	}

	private FieldType findFieldType(String columnFieldName) throws SQLException
	{
		GeneratedTableMapper generatedTableMapper = squeakyContext.getGeneratedTableMapper(c);
		FieldType[] fieldTypes = generatedTableMapper.getTableConfig().getFieldTypes();
		for (FieldType fieldType : fieldTypes)
		{
			if(fieldType.getFieldName().equalsIgnoreCase(columnFieldName) || fieldType.getColumnName().equalsIgnoreCase(columnFieldName))
			{
				return fieldType;
			}
		}

		throw new SQLException("No field type found for "+ columnFieldName);
	}
}
