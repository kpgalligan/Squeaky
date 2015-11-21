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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BigDecimalTypeTest extends BaseTypeTest
{

	private final static String BIGDECIMAL_COLUMN = "bigDecimal";
	private final static String DEFAULT_VALUE = "1.3452904234234732472343454353453453453453453453453453453";
	public static final String LOCAL_BIG_DECIMAL = "LocalBigDecimal";

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
	public void testBigDecimal() throws Exception
	{
		Class<LocalBigDecimal> clazz = LocalBigDecimal.class;
		Dao<LocalBigDecimal> dao = helper.getDao(LocalBigDecimal.class);
		BigDecimal val = new BigDecimal("1.345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigDecimal foo = new LocalBigDecimal();
		foo.bigDecimal = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));

	}

	@Test
	public void testBigDecimalNull() throws Exception
	{
		Dao<LocalBigDecimal> dao = helper.getDao(LocalBigDecimal.class);
		LocalBigDecimal foo = new LocalBigDecimal();
		dao.create(foo);

		List<LocalBigDecimal> results = dao.queryForAll().list();
		assertEquals(1, results.size());
		assertNull(results.get(0).bigDecimal);
	}

	@Test(expected = SQLException.class)
	public void testBigDecimalInvalidDbValue() throws Exception
	{
		Dao<LocalBigDecimal> dao = helper.getDao(LocalBigDecimal.class);
		Dao<NotBigDecimal> notDao = helper.getDao(NotBigDecimal.class);

		NotBigDecimal notFoo = new NotBigDecimal();
		notFoo.bigDecimal = "not valid form";
		notDao.create(notFoo);

		dao.queryForAll().list();
	}

	@DatabaseTable(tableName = LOCAL_BIG_DECIMAL)
	protected static class LocalBigDecimal
	{
		@DatabaseField(columnName = BIGDECIMAL_COLUMN)
		BigDecimal bigDecimal;
	}

	@DatabaseTable(tableName = LOCAL_BIG_DECIMAL)
	protected static class NotBigDecimal
	{
		@DatabaseField(columnName = BIGDECIMAL_COLUMN)
		String bigDecimal;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalBigDecimal.class
		);
	}
}
