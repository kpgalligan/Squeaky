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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class DateTypeTest extends BaseTypeTest
{

	private static final String DATE_COLUMN = "date";
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
	public void testDate() throws Exception
	{
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate> dao = helper.getDao(clazz);
		// we have to round to 0 millis
		long millis = System.currentTimeMillis();
		millis -= millis % 1000;
		java.util.Date val = new java.util.Date(millis);
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalDate foo = new LocalDate();
		foo.date = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testDateNull() throws Exception
	{
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate> dao = helper.getDao(clazz);
		LocalDate foo = new LocalDate();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@DatabaseTable
	protected static class LocalDate
	{
		@DatabaseField(columnName = DATE_COLUMN)
		java.util.Date date;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalDate.class
		);
	}
}
