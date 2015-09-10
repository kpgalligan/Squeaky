package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.stmt.query.Queryable;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kgalligan on 9/8/15.
 */
@RunWith(RobolectricTestRunner.class)
public class QueryTest extends BaseTypeTest
{

	private static final String DATE_COLUMN = "date";
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
	public void testBasicQuery() throws Exception
	{
		Class<Foo> clazz = Foo.class;
		Dao<Foo, Object> dao = helper.getDao(clazz);

		Where<Foo, Object> where = dao.createWhere();

		where.eq("name", "asdf");

		StmtTestHelper.assertWhere("`name` = 'asdf'", where);
		StmtTestHelper.assertWhere("`ival` = 123",
				dao.createWhere().eq("ival", 123));
		StmtTestHelper.assertWhere("`lval` = 234235234234",
				dao.createWhere().eq("lval", 234235234234l));
		StmtTestHelper.assertWhere("`dval` = 23.45234",
				dao.createWhere().eq("dval", 23.45234));
		Date now = new Date();
		String dateString = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
		StmtTestHelper.assertWhere("`sDate` = '" + dateString + "'",
				dao.createWhere().eq("sDate", now));
		StmtTestHelper.assertWhere("`lDate` = 	" + now.getTime(),
				dao.createWhere().eq("lDate", now));

		StmtTestHelper.assertWhere("(NOT `lDate` = 	" + now.getTime() + ")",
				dao.createWhere().not().eq("lDate", now));

		Where<Foo, Object> makeAnd = dao.createWhere();
		makeAnd.and().eq("name", "asdf").eq("ival", 123);
		StmtTestHelper.assertWhere("(`name` = 'asdf' AND `ival` = 123)", makeAnd);

		Queryable bigWhere =
				dao.createWhere()
						.or()
							.and()
								.eq(QueryTest$Foo$$Configuration.Fields.lval.name(), 2223424)
								.between(QueryTest$Foo$$Configuration.Fields.dval.name(), 123, 456)
							.end()
							.eq("ival", 123)
						.end();

		/*Where<Foo, Object> complexWhere = dao.createWhere();
		ManyClause or = complexWhere.or();

		or.and()
			.eq(QueryTest$Foo$$Configuration.Fields.lval.name(), 2223424)
			.between(QueryTest$Foo$$Configuration.Fields.dval.name(), 123, 456);

		or.eq("ival", 123);*/

		StmtTestHelper.assertWhere("((`lval` = 2223424 AND `dval` BETWEEN 123 AND 456 ) OR `ival` = 123 ) ", bigWhere);
//		dao.createWhere().and()
	}


	@DatabaseTable
	protected static class Foo
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@DatabaseField
		int ival;

		@DatabaseField
		long lval;

		@DatabaseField
		double dval;

		@DatabaseField
		float fval;

		@DatabaseField(dataType = DataType.DATE_STRING, format = "MM/dd/yyyy HH:mm")
		Date sDate;

		@DatabaseField(dataType = DataType.DATE_LONG)
		Date lDate;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Foo.class
		);
	}
}

