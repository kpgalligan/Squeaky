package com.j256.ormlite.field.types;

import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ByteTypeTest extends BaseTypeTest {

	private static final String BYTE_COLUMN = "byteField";
	public static final String LOCAL_BYTE = "LocalByte";
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
	public void testByte() throws Exception {
		Class<LocalByte> clazz = LocalByte.class;
		Dao<LocalByte, Object> dao = helper.getDao(clazz);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByte foo = new LocalByte();
		foo.byteField = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@Test
	public void testBytePrimitiveNull() throws Exception {
		Dao<LocalByteObj, Object> objDao = helper.getDao(LocalByteObj.class);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = null;
		objDao.create(foo);

		Dao<LocalByte, Object> dao = helper.getDao(LocalByte.class);
		List<LocalByte> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).byteField);
	}

	@Test
	public void testCoverage() {
		new ByteType(SqlType.BYTE, new Class[0]);
	}

	@DatabaseTable(tableName = LOCAL_BYTE)
	protected static class LocalByte {
		@DatabaseField(columnName = BYTE_COLUMN)
		byte byteField;
	}

	@DatabaseTable(tableName = LOCAL_BYTE)
	protected static class LocalByteObj {
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
