package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ShortTypeTest extends BaseTypeTest
{

	private static final String SHORT_COLUMN = "shortField";
	private static final String TABLE_NAME = "com_j256_ormlite_field_types_ShortTypeTest_LocalShort";
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
	public void testShort() throws Exception
	{
		Class<LocalShort> clazz = LocalShort.class;
		Dao<LocalShort> dao = helper.getDao(clazz);
		short val = 12312;
		String valStr = Short.toString(val);
		LocalShort foo = new LocalShort();
		foo.shortField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testShortPrimitiveNull() throws Exception
	{
		Dao<LocalShortObj> objDao = helper.getDao(LocalShortObj.class);
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = null;
		objDao.create(foo);
		Dao<LocalShort> dao = helper.getDao(LocalShort.class);
		List<LocalShort> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).shortField);
	}

	@Test
	public void testCoverage()
	{
		new ShortType(SqlType.SHORT, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShort
	{
		@DatabaseField(columnName = SHORT_COLUMN)
		short shortField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShortObj
	{
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalShort.class
		);
	}
}
