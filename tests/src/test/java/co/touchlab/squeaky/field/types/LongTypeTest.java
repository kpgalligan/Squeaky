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
public class LongTypeTest extends BaseTypeTest
{

	private static final String LONG_COLUMN = "longField";
	public static final String TABLE_NAME = "com_j256_ormlite_field_types_LongTypeTest_LocalLong";
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
	public void testLong() throws Exception
	{
		Class<LocalLong> clazz = LocalLong.class;
		Dao<LocalLong> dao = helper.getDao(clazz);
		long val = 13312321312312L;
		String valStr = Long.toString(val);
		LocalLong foo = new LocalLong();
		foo.longField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testLongPrimitiveNull() throws Exception
	{
		Dao<LocalLongObj> objDao = helper.getDao(LocalLongObj.class);
		LocalLongObj foo = new LocalLongObj();
		foo.longField = null;
		objDao.create(foo);
		Dao<LocalLong> dao = helper.getDao(LocalLong.class);
		List<LocalLong> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).longField);
	}

	@Test
	public void testCoverage()
	{
		new LongType(SqlType.LONG, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLong
	{
		@DatabaseField(columnName = LONG_COLUMN)
		long longField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongObj
	{
		@DatabaseField(columnName = LONG_COLUMN)
		Long longField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalLong.class
		);
	}
}
