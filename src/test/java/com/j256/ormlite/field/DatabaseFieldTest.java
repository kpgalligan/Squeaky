package com.j256.ormlite.field;

import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.field.types.BaseTypeTest;
import com.j256.ormlite.table.DatabaseTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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

	@Before
	public void after()
	{
		helper.close();
	}

	@Test
	public void testBaseClassAnnotations() throws Exception {
		Sub sub = new Sub();
		String stuff = "djeqpodjewdopjed";
		sub.stuff = stuff;

		Dao<Sub, Object> dao = helper.getDao(Sub.class);
		assertEquals(0, sub.id);
		dao.create(sub);
//		assertEquals(1, dao.create(sub));
		Sub sub2 = dao.queryForId(sub.id);
		assertNotNull(sub2);
		assertEquals(sub.stuff, sub2.stuff);
	}

	private static class Base {
		@DatabaseField(id = true)
		int id;
		public Base() {
			// for ormlite
		}
	}

	@DatabaseTable
	static class Sub extends Base {
		@DatabaseField
		String stuff;
		public Sub() {
			// for ormlite
		}
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Sub.class
		);
	}
}
