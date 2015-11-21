package co.touchlab.squeaky.field;

import android.database.Cursor;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.db.sqlite.SQLiteDatabaseImpl;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.table.DatabaseTable;
import co.touchlab.squeaky.table.TableUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class DatabaseFieldTest extends BaseTypeTest
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
	public void testColumnName()throws Exception
	{
		Dao<ColumnNameTable> dao = helper.getDao(ColumnNameTable.class);
		ColumnNameTable columnNameTable = new ColumnNameTable();
		columnNameTable.id = 1;
		columnNameTable.asdf = "jjjjj";
		dao.create(columnNameTable);

		Cursor cursor = helper.getWritableDatabase().rawQuery("select * from ColumnNameTable", null);
		Assert.assertTrue(cursor.getColumnIndex("_id") > -1);
		Assert.assertTrue(cursor.getColumnIndex("qwert") > -1);
	}

	@DatabaseTable
	static class ColumnNameTable
	{
		@DatabaseField(id = true, columnName = "_id")
		int id;

		@DatabaseField(columnName = "qwert")
		String asdf;
	}

	@Test
	public void testVariousFieldConfigs()throws Exception
	{
		List<String> sqlList = TableUtils.getCreateTableStatements(new SQLiteDatabaseImpl(helper.getWritableDatabase()), VariousFieldConfigs.class);

		Assert.assertTrue(sqlList.get(0).contains("UNIQUE (`uni`)"));
		Assert.assertTrue(sqlList.get(0).contains("UNIQUE (`uniComboA`,`uniComboB`)"));

		lookForIndex(sqlList, "funkyTown", "CREATE INDEX `funkyTown` ON `variousfieldconfigs` ( `funkyIndex` )");
		lookForIndex(sqlList, "funkyUniqueTown", "CREATE UNIQUE INDEX `funkyUniqueTown` ON `variousfieldconfigs` ( `funkyUniqueIndex` )");
	}

	private void lookForIndex(List<String> sqlList, String indexName, String createStmt)
	{
		boolean found = false;
		for (String s : sqlList)
		{
			if(s.contains(indexName))
			{
				found = true;
				Assert.assertEquals(createStmt, s);
			}
		}
		Assert.assertTrue(found);
	}

	@DatabaseTable
	static class VariousFieldConfigs
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField(defaultValue = "asdf")
		String a;

		@DatabaseField(canBeNull = false)
		String b;

		@DatabaseField(unique = true)
		String uni;

		@DatabaseField(uniqueCombo = true)
		String uniComboA;

		@DatabaseField(uniqueCombo = true)
		String uniComboB;

		@DatabaseField(index = true, indexName = "funkyTown")
		String funkyIndex;

		@DatabaseField(uniqueIndex = true, uniqueIndexName = "funkyUniqueTown")
		String funkyUniqueIndex;
	}

	@Test
	public void testBaseClassAnnotations() throws Exception
	{
		Sub sub = new Sub();
		String stuff = "djeqpodjewdopjed";
		sub.stuff = stuff;

		Dao<Sub> dao = helper.getDao(Sub.class);
		assertEquals(0, sub.id);
		dao.create(sub);
//		assertEquals(1, dao.create(sub));
		Sub sub2 = dao.queryForId(sub.id);
		assertNotNull(sub2);
		assertEquals(sub.stuff, sub2.stuff);
	}

	private static class Base
	{
		@DatabaseField(id = true)
		int id;

		public Base()
		{
			// for ormlite
		}
	}

	@DatabaseTable
	static class Sub extends Base
	{
		@DatabaseField
		String stuff;

		public Sub()
		{
			// for ormlite
		}
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Sub.class, ColumnNameTable.class, VariousFieldConfigs.class
		);
	}
}
