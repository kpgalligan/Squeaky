package com.j256.ormlite.android.apptools.basic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.android.squeaky.SqueakyOpenHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kgalligan on 7/18/15.
 */
@RunWith(RobolectricTestRunner.class)
public class BasicEntityTests
{
	@Test
	public void basicDbTest() throws SQLException
	{
		OpenHelper openHelper = new OpenHelper(RuntimeEnvironment.application);
		Dao<A, Long> dao = openHelper.getDao(A.class);
		A a = new A();
		a.name = "A test";
		dao.create(a);
		List<A> as = dao.queryForAll();

		Assert.assertEquals(a, as.get(0));

		Dao<B, Long> bDao = openHelper.getDao(B.class);

		B b = new B();
		b.name = "B test";
		bDao.create(b);

		List<B> bs = bDao.queryForAll();

		Assert.assertEquals(b, bs.get(0));
	}

	@DatabaseTable
	public static class B
	{
		@DatabaseField(generatedId = true)
		public Long id;

		@DatabaseField
		public String name;

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			B b = (B) o;

			if (id != null ? !id.equals(b.id) : b.id != null) return false;
			return !(name != null ? !name.equals(b.name) : b.name != null);

		}

		@Override
		public int hashCode()
		{
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			return result;
		}
	}

	static class OpenHelper extends SqueakyOpenHelper
	{
		public static final String NAME = BasicEntityTests.class.getName() + ".db";
		public static final int VERSION = 1;

		public OpenHelper(Context context)
		{
			super(context, NAME, null, VERSION, A.class, B.class);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(sqLiteDatabase, A.class, B.class);
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
				TableUtils.dropTables(sqLiteDatabase, true, B.class, A.class);
				onCreate(sqLiteDatabase);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
