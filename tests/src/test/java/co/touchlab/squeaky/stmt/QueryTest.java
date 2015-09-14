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


		Where<Foo, Object> where = new Where(dao);

		where.eq("name", "asdf");

		StmtTestHelper.assertWhere("t.`name` = ?", where, new String[]{"asdf"});
		StmtTestHelper.assertWhere("t.`name` = ?",
				new Where<Foo, Object>(dao).eq("name", "sdf'wfsd"),
				new String[]{"sdf'wfsd"});
		StmtTestHelper.assertWhere("t.`ival` = ?",
				new Where<Foo, Object>(dao).eq("ival", 123), new String[]{"123"});
		StmtTestHelper.assertWhere("t.`lval` = ?",
				new Where<Foo, Object>(dao).eq("lval", 234235234234l), new String[]{"234235234234"});
		StmtTestHelper.assertWhere("t.`dval` = ?",
				new Where<Foo, Object>(dao).eq("dval", 23.45234), new String[]{"23.45234"});
		Date now = new Date();
		String dateString = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
		StmtTestHelper.assertWhere("t.`sDate` = ?",
				new Where<Foo, Object>(dao).eq("sDate", now), new String[]{dateString});
		StmtTestHelper.assertWhere("t.`lDate` = ?",
				new Where<Foo, Object>(dao).eq("lDate", now), new String[]{Long.toString(now.getTime())});

		StmtTestHelper.assertWhere("(NOT t.`lDate` = ?)",
				(Where<Foo, Object>)(new Where<Foo, Object>(dao).not().eq("lDate", now)), new String[]{Long.toString(now.getTime())});

		Where<Foo, Object> makeAnd = new Where<>(dao);
		makeAnd.and().eq("name", "asdf").eq("ival", 123);
		StmtTestHelper.assertWhere("(t.`name` = ? AND t.`ival` = ?)", makeAnd, new String[]{"asdf", "123"});

		Queryable bigWhere =
				new Where<Foo, Object>(dao)
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

		StmtTestHelper.assertWhere("((t.`lval` = ? AND t.`dval` BETWEEN ? AND ? ) OR t.`ival` = ? ) ",
				(Where<Foo, Object>)bigWhere,
				new String[]{"2223424", "123", "456", "123"});
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

