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

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CharTypeTest extends BaseTypeTest
{

	private static final String CHAR_COLUMN = "charField";
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
	public void testChar() throws Exception
	{
		Class<LocalChar> clazz = LocalChar.class;
		Dao<LocalChar> dao = helper.getDao(clazz);
		char val = 'w';
		String valStr = Character.toString(val);
		LocalChar foo = new LocalChar();
		foo.charField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testCoverage()
	{
		new CharType(SqlType.CHAR, new Class[0]);
	}

	@DatabaseTable
	protected static class LocalChar
	{
		@DatabaseField(columnName = CHAR_COLUMN)
		char charField;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalChar.class
		);
	}
}
