package co.touchlab.squeaky.apptools;

import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;

/**
 * Created by kgalligan on 7/18/15.
 */
@DatabaseTable
public class BasicEntity
{
	@DatabaseField(generatedId = true)
	public Long id;

	@DatabaseField
	public String val;
}
