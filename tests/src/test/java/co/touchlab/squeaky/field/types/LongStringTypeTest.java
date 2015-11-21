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
public class LongStringTypeTest extends BaseTypeTest
{

	private static final String STRING_COLUMN = "string";
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
	public void testLongString() throws Exception
	{
		Class<LocalLongString> clazz = LocalLongString.class;
		Dao<LocalLongString> dao = helper.getDao(LocalLongString.class);
		String val = "str";
		String valStr = val;
		LocalLongString foo = new LocalLongString();
		foo.string = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testCoverage()
	{
		new LongStringType(SqlType.LONG_STRING, new Class[0]);
	}

	@DatabaseTable
	protected static class LocalLongString
	{
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.LONG_STRING)
		String string;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalLongString.class
		);
	}
}
