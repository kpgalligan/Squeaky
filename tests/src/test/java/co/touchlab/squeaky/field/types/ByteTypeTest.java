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
public class ByteTypeTest extends BaseTypeTest
{

	private static final String BYTE_COLUMN = "byteField";
	public static final String LOCAL_BYTE = "LocalByte";
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
	public void testByte() throws Exception
	{
		Class<LocalByte> clazz = LocalByte.class;
		Dao<LocalByte> dao = helper.getDao(clazz);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByte foo = new LocalByte();
		foo.byteField = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testBytePrimitiveNull() throws Exception
	{
		Dao<LocalByteObj> objDao = helper.getDao(LocalByteObj.class);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = null;
		objDao.create(foo);

		Dao<LocalByte> dao = helper.getDao(LocalByte.class);
		List<LocalByte> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).byteField);
	}

	@Test
	public void testCoverage()
	{
		new ByteType(SqlType.BYTE, new Class[0]);
	}

	@DatabaseTable(tableName = LOCAL_BYTE)
	protected static class LocalByte
	{
		@DatabaseField(columnName = BYTE_COLUMN)
		byte byteField;
	}

	@DatabaseTable(tableName = LOCAL_BYTE)
	protected static class LocalByteObj
	{
		@DatabaseField(columnName = BYTE_COLUMN)
		Byte byteField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalByte.class
		);
	}
}
