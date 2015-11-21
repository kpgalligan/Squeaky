package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class LongObjectTypeTest extends BaseTypeTest
{

	private static final String LONG_COLUMN = "longField";
	public static final String TABLE_NAME = "com_j256_ormlite_field_types_LongObjectTypeTest_table";
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
	public void testLongObj() throws Exception
	{
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj> dao = helper.getDao(clazz);
		Long val = 13312321312312L;
		String valStr = val.toString();
		LocalLongObj foo = new LocalLongObj();
		foo.longField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testLongObjNull() throws Exception
	{
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj> dao = helper.getDao(clazz);
		LocalLongObj foo = new LocalLongObj();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
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
				LocalLongObj.class
		);
	}
}
