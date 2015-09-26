package co.touchlab.squeaky.field;

/**
 * Created by kgalligan on 7/19/15.
 */
public final class OrmLiteHelper
{
	/**
	 * Safe convert val
	 */
	public static Object safeConvert(Class type, Object arg)
	{
		if (int.class.equals(type) || Integer.class.equals(type))
		{
			return ((Number) arg).intValue();
		}
		else if (long.class.equals(type) || Long.class.equals(type))
		{
			return ((Number) arg).longValue();
		}
		else if (short.class.equals(type) || Short.class.equals(type))
		{
			return ((Number) arg).shortValue();
		}
		else
		{
			return arg;
		}
	}
}

