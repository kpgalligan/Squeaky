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
public class IntTypeTest extends BaseTypeTest
{

	private static final String INT_COLUMN = "intField";
	public static final String TABLE_NAME = "com_j256_ormlite_field_types_IntTypeTest_table";
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
	public void testInt() throws Exception
	{
		Class<LocalInt> clazz = LocalInt.class;
		Dao<LocalInt> dao = helper.getDao(clazz);
		int val = 313213123;
		String valStr = Integer.toString(val);
		LocalInt foo = new LocalInt();
		foo.intField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testIntPrimitiveNull() throws Exception
	{
		Dao<LocalIntObj> objDao = helper.getDao(LocalIntObj.class);
		LocalIntObj foo = new LocalIntObj();
		foo.intField = null;
		objDao.create(foo);
		// overlapping table
		Dao<LocalInt> dao = helper.getDao(LocalInt.class);
		List<LocalInt> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).intField);
	}

	@Test
	public void testCoverage()
	{
		new IntType(SqlType.INTEGER, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalIntObj
	{
		@DatabaseField(columnName = INT_COLUMN)
		Integer intField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalInt
	{
		@DatabaseField(columnName = INT_COLUMN)
		int intField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalIntObj.class
		);
	}
}
