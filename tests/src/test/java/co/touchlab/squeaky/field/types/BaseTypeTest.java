package co.touchlab.squeaky.field.types;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.db.sqlite.SQLiteDatabaseImpl;
import co.touchlab.squeaky.db.sqlite.SqueakyOpenHelper;
import co.touchlab.squeaky.table.TableUtils;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public abstract class BaseTypeTest
{

	public SimpleHelper createHelper(Class... c)
	{
		return new SimpleHelper(RuntimeEnvironment.application, getClass().getSimpleName() + ".db", c);
	}

	public SimpleHelper createViewHelper(String viewSql, Class... c)
	{
		return new SimpleHelper(RuntimeEnvironment.application, Collections.singletonList(viewSql), getClass().getSimpleName() + ".db", c);
	}

	public static class SimpleHelper extends SqueakyOpenHelper
	{
		private final Class[] managingClasses;
		private List<String> createSqlList;

		public SimpleHelper(Context context, String name, Class... managingClasses)
		{
			super(context, name, null, 1);
			this.managingClasses = managingClasses;
		}

		public SimpleHelper(Context context, List<String> createSqlList, String name, Class... managingClasses)
		{

			super(context, name, null, 1);
			this.managingClasses = managingClasses;
			this.createSqlList = createSqlList;
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(new SQLiteDatabaseImpl(sqLiteDatabase), managingClasses);
				if (createSqlList != null)
				{
					for (String s : createSqlList)
					{
						sqLiteDatabase.execSQL(s);
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
		{
			Class[] reversed = new Class[managingClasses.length];

			for (int i = 0; i < managingClasses.length; i++)
			{
				reversed[(managingClasses.length - i) - 1] = managingClasses[i];
			}
			try
			{

				TableUtils.dropTables(new SQLiteDatabaseImpl(sqLiteDatabase), true, reversed);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
