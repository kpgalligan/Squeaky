package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;

/**
 * Marker class used to see if we have a customer persister defined.
 *
 * @author graywatson
 */
public class VoidType extends BaseDataType
{

	VoidType()
	{
		super(null, new Class<?>[]{});
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr)
	{
		return null;
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos)
	{
		return null;
	}
}
