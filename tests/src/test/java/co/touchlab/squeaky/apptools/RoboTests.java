package co.touchlab.squeaky.apptools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.SqueakyOpenHelper;
import co.touchlab.squeaky.table.TableUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;

/**
 * Created by kgalligan on 7/18/15.
 */
@RunWith(RobolectricTestRunner.class)
public class RoboTests
{
	@Test
	public void basicDbTest() throws SQLException
	{
		System.out.println(RuntimeEnvironment.application.getDatabasePath(BasicOpenHelper.BASIC_DB));
		BasicOpenHelper basicOpenHelper = new BasicOpenHelper(RuntimeEnvironment.application);
		Dao dao = basicOpenHelper.getDao(BasicEntity.class);
		BasicEntity basicEntity = new BasicEntity();
		basicEntity.val = "thetest";
		dao.create(basicEntity);
		basicOpenHelper.close();
	}

	static class BasicOpenHelper extends SqueakyOpenHelper
	{

		public static final String BASIC_DB = "basic.db";
		public static final int VERSION = 1;

		public BasicOpenHelper(Context context)
		{
			super(context, BASIC_DB, null, VERSION, new Class[]{BasicEntity.class});
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(sqLiteDatabase, BasicEntity.class);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
		{
			try
			{
				TableUtils.dropTables(sqLiteDatabase, true, BasicEntity.class);
				onCreate(sqLiteDatabase);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
