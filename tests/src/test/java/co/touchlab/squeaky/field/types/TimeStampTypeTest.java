package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
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
import java.util.GregorianCalendar;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TimeStampTypeTest extends BaseTypeTest
{

	private static final String TIME_STAMP_COLUMN = "timestamp";
	private DataType dataType = DataType.TIME_STAMP;
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
	public void testTimeStamp() throws Exception
	{
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp> dao = helper.getDao(clazz);
		GregorianCalendar c = new GregorianCalendar();
		c.set(GregorianCalendar.MILLISECOND, 0);
		long millis = c.getTimeInMillis();
		java.sql.Timestamp val = new java.sql.Timestamp(millis);
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalTimeStamp foo = new LocalTimeStamp();
		foo.timestamp = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testTimeStampNull() throws Exception
	{
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp> dao = helper.getDao(clazz);
		LocalTimeStamp foo = new LocalTimeStamp();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	/*@Test
	public void testTimeStampDefault() throws Exception {
		Dao<TimeStampDefault> dao = helper.getDao(TimeStampDefault.class);
		TimeStampDefault foo = new TimeStampDefault();
		Timestamp before = new Timestamp(System.currentTimeMillis());
		Thread.sleep(1);
		dao.create(foo);
		Thread.sleep(1);
		Timestamp after = new Timestamp(System.currentTimeMillis());

		TimeStampDefault result = dao.queryForId(foo.id);
		assertTrue(result.timestamp.after(before));
		assertTrue(result.timestamp.before(after));
	}*/

	@DatabaseTable
	protected static class LocalTimeStamp
	{
		@DatabaseField(columnName = TIME_STAMP_COLUMN)
		java.sql.Timestamp timestamp;
	}

	/*@DatabaseTable
	protected static class TimeStampDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = TIME_STAMP_COLUMN, persisterClass = LocalCurrentTimeStampType.class,
				defaultValue = "CURRENT_TIMESTAMP()", readOnly = true)
		java.sql.Timestamp timestamp;
		@DatabaseField
		String stuff;
	}*/

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalTimeStamp.class
		);
	}

	/*protected static class LocalCurrentTimeStampType extends TimeStampType {
		private static final LocalCurrentTimeStampType singleton = new LocalCurrentTimeStampType();
		private String defaultStr;
		public LocalCurrentTimeStampType() {
			super(SqlType.DATE, new Class<?>[] { java.sql.Timestamp.class });
		}
		public static LocalCurrentTimeStampType getSingleton() {
			return singleton;
		}
		@Override
		public boolean isEscapedDefaultValue() {
			if ("CURRENT_TIMESTAMP()".equals(defaultStr)) {
				return false;
			} else {
				return super.isEscapedDefaultValue();
			}
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			this.defaultStr = defaultStr;
			if ("CURRENT_TIMESTAMP()".equals(defaultStr)) {
				return defaultStr;
			} else {
				return super.parseDefaultString(fieldType, defaultStr);
			}
		}
	}*/
}
