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
public class DoubleTypeTest extends BaseTypeTest
{

	private static final String DOUBLE_COLUMN = "doubleField";
	public static final String LOCAL_DOUBLE = "LocalDouble";
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
	public void testDouble() throws Exception
	{
		Class<LocalDouble> clazz = LocalDouble.class;
		Dao<LocalDouble> dao = helper.getDao(clazz);
		double val = 13313323131.221;
		String valStr = Double.toString(val);
		LocalDouble foo = new LocalDouble();
		foo.doubleField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testDoublePrimitiveNull() throws Exception
	{
		Dao<LocalDoubleObj> objDao = helper.getDao(LocalDoubleObj.class);
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = null;
		objDao.create(foo);

		Dao<LocalDouble> dao = helper.getDao(LocalDouble.class);
		List<LocalDouble> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).doubleField, 0.0F);
	}

	@Test
	public void testCoverage()
	{
		new DoubleType(SqlType.DOUBLE, new Class[0]);
	}

	@DatabaseTable(tableName = LOCAL_DOUBLE)
	protected static class LocalDouble
	{
		@DatabaseField(columnName = DOUBLE_COLUMN)
		double doubleField;
		;
	}

	@DatabaseTable(tableName = LOCAL_DOUBLE)
	protected static class LocalDoubleObj
	{
		@DatabaseField(columnName = DOUBLE_COLUMN)
		Double doubleField;
		;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalDouble.class
		);
	}
}
