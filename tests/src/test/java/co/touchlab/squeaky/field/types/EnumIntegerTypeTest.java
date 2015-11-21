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
public class EnumIntegerTypeTest extends BaseTypeTest
{

	private static final String ENUM_COLUMN = "ourEnum";
	private static final String TABLE_NAME = "com_j256_ormlite_field_types_EnumIntegerTypeTest_LocalEnumInt";
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
	public void testEnumInt() throws Exception
	{
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt> dao = helper.getDao(clazz);
		OurEnum val = OurEnum.SECOND;
		int sqlVal = val.ordinal();
		String valStr = Integer.toString(sqlVal);
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testEnumIntNull() throws Exception
	{
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt> dao = helper.getDao(clazz);
		LocalEnumInt foo = new LocalEnumInt();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	/*@Test
	public void testEnumIntResultsNoFieldType() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt =
					conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			assertEquals(
					val.ordinal(),
					DataType.ENUM_INTEGER.getDataPersister().resultToJava(null, results,
							results.findColumn(ENUM_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}*/

	@Test
	public void testCoverage()
	{
		new EnumIntegerType(SqlType.INTEGER, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumInt
	{
		@DatabaseField(columnName = ENUM_COLUMN, dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnum;
	}

	enum OurEnum
	{
		FIRST,
		SECOND,;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalEnumInt.class
		);
	}
}
