package co.touchlab.squeaky.apptools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.dao.SqueakyOpenHelper;
import co.touchlab.squeaky.table.TableUtils;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;

/**
 * Created by kgalligan on 7/19/15.
 */
public class BaseTest
{
	SimpleHelper createHelper(Class... c)
	{
		return new SimpleHelper(RuntimeEnvironment.application, getClass().getSimpleName() + ".db", c);
	}

	public static class SimpleHelper extends SqueakyOpenHelper
	{

		public SimpleHelper(Context context, String name, Class... managingClasses)
		{
			super(context, name, null, 1, managingClasses);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(sqLiteDatabase, getManagingClasses());
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
		{
			Class[] managingClasses = getManagingClasses();
			Class[] reversed = new Class[managingClasses.length];

			for (int i = 0; i < managingClasses.length; i++)
			{
				reversed[(managingClasses.length - i) - 1] = managingClasses[i];
			}
			try
			{
				TableUtils.dropTables(sqLiteDatabase, true, reversed);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
