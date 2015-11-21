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

import java.util.Date;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class DateLongTypeTest extends BaseTypeTest
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
	public void testDateLong() throws Exception
	{
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong> dao = helper.getDao(clazz);
		Date val = new Date();
		long sqlVal = val.getTime();
		String valStr = Long.toString(val.getTime());
		LocalDateLong foo = new LocalDateLong();
		foo.date = val;
		dao.create(foo);

		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testDateLongNull() throws Exception
	{
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong> dao = helper.getDao(clazz);
		LocalDateLong foo = new LocalDateLong();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testCoverage()
	{
		new DateLongType(SqlType.LONG, new Class[0]);
	}

	@DatabaseTable
	protected static class LocalDateLong// extends BaseTablee
	{
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_LONG)
		Date date;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalDateLong.class
		);
	}
}
