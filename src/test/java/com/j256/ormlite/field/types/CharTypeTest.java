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

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CharTypeTest extends BaseTypeTest {

	private static final String CHAR_COLUMN = "charField";
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
	public void testChar() throws Exception {
		Class<LocalChar> clazz = LocalChar.class;
		Dao<LocalChar, Object> dao = helper.getDao(clazz);
		char val = 'w';
		String valStr = Character.toString(val);
		LocalChar foo = new LocalChar();
		foo.charField = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().get(0)));
	}

	@Test
	public void testCoverage() {
		new CharType(SqlType.CHAR, new Class[0]);
	}

	@DatabaseTable
	protected static class LocalChar {
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
