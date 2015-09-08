package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.android.squeaky.Dao;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FloatObjectTypeTest extends BaseTypeTest {

	private static final String FLOAT_COLUMN = "floatField";
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
	public void testFloatObj() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = helper.getDao(clazz);
		Float val = 1331.221F;
		String valStr = val.toString();
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@Test
	public void testFloatObjNull() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = helper.getDao(clazz);
		LocalFloatObj foo = new LocalFloatObj();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@DatabaseTable
	protected static class LocalFloatObj {
		@DatabaseField(columnName = FLOAT_COLUMN)
		Float floatField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalFloatObj.class
		);
	}
}
