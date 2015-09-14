package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.field.FieldType;

/**
 * Created by kgalligan on 9/13/15.
 */
public class JoinAlias
{
	public final Class tableType;
	public final String tablePrefix;
	public final FieldType fieldType;

	public JoinAlias(Class tableType, String tablePrefix, FieldType fieldType)
	{
		this.tableType = tableType;
		this.tablePrefix = tablePrefix;
		this.fieldType = fieldType;
	}
}
