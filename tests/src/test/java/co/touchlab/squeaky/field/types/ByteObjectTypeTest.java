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
public class ByteObjectTypeTest extends BaseTypeTest
{

	private static final String BYTE_COLUMN = "byteField";
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
	public void testByteObj() throws Exception
	{
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj> dao = helper.getDao(clazz);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testByteObjNull() throws Exception
	{
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj> dao = helper.getDao(clazz);
		LocalByteObj foo = new LocalByteObj();
		dao.create(new LocalByteObj());
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable
	protected static class LocalByteObj
	{
		@DatabaseField(columnName = BYTE_COLUMN)
		Byte byteField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalByteObj.class
		);
	}
}
