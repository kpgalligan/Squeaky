package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.field.FieldType;

import java.sql.SQLException;

/**
 * Created by kgalligan on 9/13/15.
 */
public class JoinAlias
{
	public final Where where;
	public final String parentPrefix;
	public final Class tableType;
	public final String tablePrefix;
	public final FieldType fieldType;

	public JoinAlias(Where where, String parentPrefix, Class tableType, String tablePrefix, FieldType fieldType)
	{
		this.where = where;
		this.parentPrefix = parentPrefix;
		this.tableType = tableType;
		this.tablePrefix = tablePrefix;
		this.fieldType = fieldType;
	}

	public JoinAlias join(String field) throws SQLException
	{
		return where.join(this, field);
	}

}
