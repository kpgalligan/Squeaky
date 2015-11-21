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
public class CharObjectTypeTest extends BaseTypeTest
{

	private static final String CHAR_COLUMN = "charField";
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
	public void testCharObj() throws Exception
	{
		Class<LocalCharObj> clazz = LocalCharObj.class;
		Dao<LocalCharObj> dao = helper.getDao(clazz);
		Character val = 'w';
		String valStr = val.toString();
		LocalCharObj foo = new LocalCharObj();
		foo.charField = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testCharObjNull() throws Exception
	{
		Class<LocalCharObj> clazz = LocalCharObj.class;
		Dao<LocalCharObj> dao = helper.getDao(clazz);
		LocalCharObj foo = new LocalCharObj();
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable
	protected static class LocalCharObj
	{
		@DatabaseField(columnName = CHAR_COLUMN)
		Character charField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalCharObj.class
		);
	}
}
