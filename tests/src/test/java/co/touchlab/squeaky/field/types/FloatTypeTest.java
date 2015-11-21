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
public class FloatTypeTest extends BaseTypeTest
{

	private static final String FLOAT_COLUMN = "floatField";
	private SimpleHelper helper;
	public static final String TABLE_NAME = "com_j256_ormlite_field_types_FloatTypeTest_table";

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
	public void testFloat() throws Exception
	{
		Class<LocalFloat> clazz = LocalFloat.class;
		Dao<LocalFloat> dao = helper.getDao(clazz);
		float val = 1331.221F;
		String valStr = Float.toString(val);
		LocalFloat foo = new LocalFloat();
		foo.floatField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testFloatPrimitiveNull() throws Exception
	{
		Dao<LocalFloatObj> objDao = helper.getDao(LocalFloatObj.class);
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = null;
		objDao.create(foo);
		Dao<LocalFloat> dao = helper.getDao(LocalFloat.class);
		List<LocalFloat> all = dao.queryForAll().list();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).floatField, 0.0F);
	}

	@Test
	public void testCoverage()
	{
		new FloatType(SqlType.FLOAT, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloat
	{
		@DatabaseField(columnName = FLOAT_COLUMN)
		float floatField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloatObj
	{
		@DatabaseField(columnName = FLOAT_COLUMN)
		Float floatField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalFloat.class
		);
	}
}
