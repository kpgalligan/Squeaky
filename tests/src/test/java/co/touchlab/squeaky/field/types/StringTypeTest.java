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
public class StringTypeTest extends BaseTypeTest
{

	private static final String STRING_COLUMN = "string";
	private static final String TABLE_NAME = "com_j256_ormlite_field_types_StringTypeTest_LocalString";
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
	public void testString() throws Exception
	{
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString> dao = helper.getDao(clazz);
		String val = "str";
		String valStr = val;
		LocalString foo = new LocalString();
		foo.string = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalString
	{
		@DatabaseField(columnName = STRING_COLUMN)
		String string;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalString.class
		);
	}
}
