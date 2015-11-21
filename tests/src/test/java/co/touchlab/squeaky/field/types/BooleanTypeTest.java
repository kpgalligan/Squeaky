package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.After;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
public class BooleanTypeTest extends BaseTypeTest
{

	private static final String BOOLEAN_COLUMN = "bool";
	public static final String LOCAL_BOOLEAN = "LocalBoolean";

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
	public void testBoolean() throws Exception
	{
		Class<LocalBoolean> clazz = LocalBoolean.class;
		Dao<LocalBoolean> dao = helper.getDao(clazz);
		boolean val = true;
		String valStr = Boolean.toString(val);
		LocalBoolean foo = new LocalBoolean();
		foo.bool = val;
		dao.create(foo);


	}

	@Test
	public void testBooleanPrimitiveNull() throws Exception
	{
		Dao<LocalBooleanObj> objDao = helper.getDao(LocalBooleanObj.class);
		LocalBooleanObj foo = new LocalBooleanObj();
		foo.bool = null;
		objDao.create(foo);

		Dao<LocalBoolean> dao = helper.getDao(LocalBoolean.class);
		List<LocalBoolean> all = dao.queryForAll().list();

		assertEquals(1, all.size());
		assertFalse(all.get(0).bool);
	}

	@Test
	public void testCoverage()
	{
		new BooleanType(SqlType.BOOLEAN, new Class[0]);
	}

	@DatabaseTable(tableName = LOCAL_BOOLEAN)
	protected static class LocalBoolean
	{
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		boolean bool;
	}

	@DatabaseTable(tableName = LOCAL_BOOLEAN)
	protected static class LocalBooleanObj
	{
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		Boolean bool;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalBoolean.class
		);
	}
}
