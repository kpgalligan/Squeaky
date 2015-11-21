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
public class IntegerObjectTypeTest extends BaseTypeTest
{

	private static final String INT_COLUMN = "intField";
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
	public void testIntObj() throws Exception
	{
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj> dao = helper.getDao(clazz);
		Integer val = 313213123;
		String valStr = val.toString();
		LocalIntObj foo = new LocalIntObj();
		foo.intField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testIntObjNull() throws Exception
	{
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj> dao = helper.getDao(clazz);
		LocalIntObj foo = new LocalIntObj();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable
	protected static class LocalIntObj
	{
		@DatabaseField(columnName = INT_COLUMN)
		Integer intField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalIntObj.class
		);
	}
}
