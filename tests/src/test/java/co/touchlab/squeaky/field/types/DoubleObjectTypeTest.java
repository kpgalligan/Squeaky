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
public class DoubleObjectTypeTest extends BaseTypeTest
{

	private static final String DOUBLE_COLUMN = "doubleField";
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
	public void testDoubleObj() throws Exception
	{
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj> dao = helper.getDao(clazz);
		Double val = 13313323131.221;
		String valStr = val.toString();
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testDoubleObjNull() throws Exception
	{
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj> dao = helper.getDao(clazz);
		LocalDoubleObj foo = new LocalDoubleObj();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable
	protected static class LocalDoubleObj
	{
		@DatabaseField(columnName = DOUBLE_COLUMN)
		Double doubleField;
		;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalDoubleObj.class
		);
	}
}
