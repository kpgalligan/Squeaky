package co.touchlab.squeaky.dao;

/**
 * Created by kgalligan on 10/15/15.
 */
public class DaoHelper
{
	public static Dao.ForeignRefresh findRefresh(Dao.ForeignRefresh[] foreignRefreshs, String fieldName)
	{
		for (Dao.ForeignRefresh foreignRefresh : foreignRefreshs)
		{
			if(foreignRefresh.field.equals(fieldName))
				return foreignRefresh;
		}

		return null;
	}
}
