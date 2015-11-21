package co.touchlab.squeaky.experiments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;
import java.util.Random;

/**
 * This should be removed.  Just playing around with ideas.
 *
 * Created by kgalligan on 10/24/15.
 */
@RunWith(RobolectricTestRunner.class)
public class InnerQueryTest extends BaseTypeTest
{
	private SimpleHelper helper;

	@Before
	public void before()
	{
		helper = getHelper();
	}

	@After
	public void after()
	{
		helper.close();
	}

	@Test
	public void testBigDecimal() throws Exception
	{
		SQLiteDatabase writableDatabase = helper.getWritableDatabase();
		writableDatabase.beginTransaction();
		try
		{
			Dao<Parent> parentDao = helper.getDao(Parent.class);
			Dao<Child> childDao = helper.getDao(Child.class);
			Random random = new Random();
			for(int i=0; i<1000; i++)
			{
				Parent parent = new Parent();
				parent.name = "parent "+ i;
				parent.category = "category "+ i;
				parent.otherShit = "other "+ i;
				parentDao.create(parent);

				for(int j = 0; j<10; j++)
				{
					Child child = new Child();
					child.someVal = "some " + i +"/"+ j;
					child.otherVal = random.nextInt();
					child.moreWeight = new Date();
					child.parent = parent;
					childDao.create(child);
				}
			}

			writableDatabase.setTransactionSuccessful();
		}
		finally
		{
			writableDatabase.endTransaction();
		}



		runQuery(writableDatabase);
	}

	private void runQuery(SQLiteDatabase db)
	{
		int mid = Integer.MAX_VALUE / 2;
		long start = System.currentTimeMillis();
		Cursor straightCursor = db.rawQuery("select * from child c left join parent p on c.parent_id = p.id " +
				"where otherVal >= ?", new String[]{Integer.toString(mid)});
		long afterFirst = System.currentTimeMillis();
		Cursor innerCursor = db.rawQuery("select * from child c left join parent p on c.parent_id = p.id where c.id in " +
				"(select id from child where otherVal >= ?)", new String[]{Integer.toString(mid)});
		long end = System.currentTimeMillis();

		System.out.println("ugh");
	}

	@DatabaseTable
	static class Child
	{
		@DatabaseField(generatedId = true)
		Long id;

		@DatabaseField
		String someVal;

		@DatabaseField
		int otherVal;

		@DatabaseField
		Date moreWeight;

		@DatabaseField(foreign = true)
		Parent parent;
	}

	@DatabaseTable
	static class Parent
	{
		@DatabaseField(generatedId = true)
		long id;

		@DatabaseField
		String name;

		@DatabaseField
		String category;

		@DatabaseField
		String otherShit;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Parent.class, Child.class
		);
	}
}
