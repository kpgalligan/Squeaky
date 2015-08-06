package com.j256.ormlite.field.types;

import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ShortObjectTypeTest extends BaseTypeTest {

	private static final String SHORT_COLUMN = "shortField";
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
	public void testShortObj() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = helper.getDao(clazz);
		Short val = 12312;
		String valStr = val.toString();
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@Test
	public void testShortObjNull() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = helper.getDao(clazz);
		LocalShortObj foo = new LocalShortObj();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@DatabaseTable
	protected static class LocalShortObj {
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalShortObj.class
		);
	}
}