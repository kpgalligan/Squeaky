package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class StringBytesTypeTest extends BaseTypeTest
{

	private static final String STRING_COLUMN = "string";
	private static final String TABLE_NAME = "com_j256_ormlite_field_types_StringBytesTypeTest_LocalStringBytes";
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
	public void testStringBytes() throws Exception
	{
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes> dao = helper.getDao(clazz);
		String val = "string with \u0185";
		LocalStringBytes foo = new LocalStringBytes();
		foo.string = val;
		dao.create(foo);
		byte[] valBytes = val.getBytes("Unicode");
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testStringBytesFormat() throws Exception
	{
		Class<LocalStringBytesUtf8> clazz = LocalStringBytesUtf8.class;
		Dao<LocalStringBytesUtf8> dao = helper.getDao(clazz);
		String val = "string with \u0185";
		LocalStringBytesUtf8 foo = new LocalStringBytesUtf8();
		foo.string = val;
		dao.create(foo);
		byte[] valBytes = val.getBytes("UTF-8");
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testStringBytesNull() throws Exception
	{
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes> dao = helper.getDao(clazz);
		LocalStringBytes foo = new LocalStringBytes();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testCoverage()
	{
		new StringBytesType(SqlType.BYTE_ARRAY, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytes
	{
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytesUtf8
	{
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES, format = "UTF-8")
		String string;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalStringBytes.class
		);
	}
}
