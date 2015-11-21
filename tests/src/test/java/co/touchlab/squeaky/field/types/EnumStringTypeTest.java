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

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class EnumStringTypeTest extends BaseTypeTest
{

	private static final String ENUM_COLUMN = "ourEnum";
	private static final String TABLE_NAME = "com_j256_ormlite_field_types_EnumStringTypeTest_LocalEnumString";
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
	public void testEnumString() throws Exception
	{
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString> dao = helper.getDao(clazz);
		OurEnum val = OurEnum.SECOND;
		String valStr = val.toString();
		String sqlVal = valStr;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testEnumStringNull() throws Exception
	{
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString> dao = helper.getDao(clazz);
		LocalEnumString foo = new LocalEnumString();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	/*@Test
	public void testEnumStringResultsNoFieldType() throws Exception {
		Dao<LocalEnumString> dao = createDao(LocalEnumString.class, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumString foo = new LocalEnumString();
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
			assertEquals(val.toString(),
					DataType.ENUM_STRING.getDataPersister()
							.resultToJava(null, results, results.findColumn(ENUM_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}*/

	@Test(expected = SQLException.class)
	public void testUnknownEnumValue() throws Exception
	{
		Dao<LocalEnumString> dao = helper.getDao(LocalEnumString.class);
		LocalEnumString localEnumString = new LocalEnumString();
		localEnumString.ourEnum = OurEnum.FIRST;
		dao.create(localEnumString);
		helper.getWritableDatabase().execSQL("UPDATE " + TABLE_NAME + " set ourEnum = 'THIRD'");

		dao.queryForAll().list();
	}

	@Test
	public void testCoverage()
	{
		new EnumStringType(SqlType.STRING, new Class[0]);
	}

	/* ================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumString
	{
		@DatabaseField(columnName = ENUM_COLUMN)
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
				LocalEnumString.class
		);
	}
}
