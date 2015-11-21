package co.touchlab.squeaky.old;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.db.sqlite.SQLiteDatabaseImpl;
import co.touchlab.squeaky.db.sqlite.SqueakyOpenHelper;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;
import co.touchlab.squeaky.table.TableUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.Date;
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
		System.out.println(RuntimeEnvironment.application.getDatabasePath(OpenHelper.NAME));

		OpenHelper openHelper = new OpenHelper(RuntimeEnvironment.application);
		{
			Dao<A> dao = openHelper.getDao(A.class);
			A a = new A();
			a.name = "A test";
			dao.create(a);
			List<A> as = dao.queryForAll().list();

			Assert.assertEquals(a, as.get(0));
		}

		{
			Dao<BPackage> bDao = openHelper.getDao(BPackage.class);

			BPackage b = new BPackage(443);
			b.name = "B test";
			bDao.create(b);

			List<BPackage> bs = bDao.queryForAll().list();

			Assert.assertEquals(b, bs.get(0));
		}

		{
			Dao<CProtected> cDao = openHelper.getDao(CProtected.class);

			CProtected c = new CProtected();
			c.name = "C test";
			cDao.create(c);

			List<CProtected> bs = cDao.queryForAll().list();

			Assert.assertEquals(c, bs.get(0));
		}

		{
			Dao<DFinal> dao = openHelper.getDao(DFinal.class);

			DFinal c = new DFinal(22, new Date(), "Final 22");

			dao.create(c);

			List<DFinal> bs = dao.queryForAll().list();

			Assert.assertEquals(c, bs.get(0));
		}
	}

	@DatabaseTable
	static class BPackage
	{
		@DatabaseField(generatedId = true)
		Long id;

		@DatabaseField
		String name;

		@DatabaseField
		final int fVal;

		BPackage(int fVal)
		{
			this.fVal = fVal;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BPackage bPackage = (BPackage) o;

			if (fVal != bPackage.fVal) return false;
			if (id != null ? !id.equals(bPackage.id) : bPackage.id != null) return false;
			return !(name != null ? !name.equals(bPackage.name) : bPackage.name != null);

		}

		@Override
		public int hashCode()
		{
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + fVal;
			return result;
		}
	}

	@DatabaseTable
	protected static class CProtected
	{
		@DatabaseField(generatedId = true)
		protected Long id;

		@DatabaseField
		protected String name;

		@DatabaseField
		protected int fVal;

		protected CProtected()
		{
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			CProtected that = (CProtected) o;

			if (fVal != that.fVal) return false;
			if (id != null ? !id.equals(that.id) : that.id != null) return false;
			return !(name != null ? !name.equals(that.name) : that.name != null);

		}

		@Override
		public int hashCode()
		{
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + fVal;
			return result;
		}
	}

	@DatabaseTable
	public static class DFinal
	{
		@DatabaseField(id = true)
		public final int id;

		@DatabaseField
		public final String name;

		@DatabaseField
		public final Date now;

		public DFinal(int id, Date now, String name)
		{
			this.id = id;
			this.name = name;
			this.now = now;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			DFinal dFinal = (DFinal) o;

			if (id != dFinal.id) return false;
			if (name != null ? !name.equals(dFinal.name) : dFinal.name != null) return false;
			return !(now != null ? !now.equals(dFinal.now) : dFinal.now != null);

		}

		@Override
		public int hashCode()
		{
			int result = id;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + (now != null ? now.hashCode() : 0);
			return result;
		}
	}

	static class OpenHelper extends SqueakyOpenHelper
	{
		public static final String NAME = BasicEntityTests.class.getName() + ".db";
		public static final int VERSION = 1;

		public OpenHelper(Context context)
		{
			super(context, NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(new SQLiteDatabaseImpl(sqLiteDatabase), A.class, BPackage.class, CProtected.class);
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
				TableUtils.dropTables(new SQLiteDatabaseImpl(sqLiteDatabase), true, CProtected.class, BPackage.class, A.class);
				onCreate(sqLiteDatabase);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
