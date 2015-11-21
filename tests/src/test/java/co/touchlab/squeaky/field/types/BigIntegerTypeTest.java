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

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BigIntegerTypeTest extends BaseTypeTest
{

	private final static String BIGINTEGER_COLUMN = "bigInteger";
	private final static String DEFAULT_VALUE = "4724724378237982347983478932478923478934789342473892342789";
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
	public void testBigInteger() throws Exception
	{
		Dao<LocalBigInteger> dao = helper.getDao(LocalBigInteger.class);
		BigInteger val =
				new BigInteger(
						"324234234234234234234234246467647647463345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigInteger foo = new LocalBigInteger();
		foo.bigInteger = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));


//		testType(dao, foo, clazz, val, valStr, valStr, valStr, DataType.BIG_INTEGER, BIGINTEGER_COLUMN, false, false,
//				true, false, false, false, true, false);
	}

	@Test
	public void testBigIntegerNull() throws Exception
	{
		Dao<LocalBigInteger> dao = helper.getDao(LocalBigInteger.class);
		LocalBigInteger foo = new LocalBigInteger();
		dao.create(foo);

		List<LocalBigInteger> results = dao.queryForAll().list();
		assertEquals(1, results.size());
		assertNull(results.get(0).bigInteger);
	}

	@Test(expected = SQLException.class)
	public void testBigIntegerInvalidDbValue() throws Exception
	{
		Dao<LocalBigInteger> dao = helper.getDao(LocalBigInteger.class);
		Dao<NotBigInteger> notDao = helper.getDao(NotBigInteger.class);

		NotBigInteger notFoo = new NotBigInteger();
		notFoo.bigInteger = "not valid form";
		notDao.create(notFoo);

		dao.queryForAll().list();
	}

	@Test
	public void testCoverage()
	{
		new BigIntegerType(SqlType.BIG_DECIMAL, new Class[0]);
	}

	@DatabaseTable(tableName = "LocalBigInteger")
	protected static class LocalBigInteger
	{
		@DatabaseField(columnName = BIGINTEGER_COLUMN)
		BigInteger bigInteger;
	}

	@DatabaseTable(tableName = "LocalBigInteger")
	protected static class NotBigInteger
	{
		@DatabaseField(columnName = BIGINTEGER_COLUMN)
		String bigInteger;
	}

	@DatabaseTable
	protected static class BigIntegerBadDefault
	{
		@DatabaseField(defaultValue = "not valid form")
		BigInteger bigInteger;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalBigInteger.class
//				BigIntegerBadDefault.class,
//				NotBigInteger.class,
		);
	}
}
