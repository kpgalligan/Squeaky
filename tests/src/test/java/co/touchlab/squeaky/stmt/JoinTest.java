package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by kgalligan on 9/13/15.
 */
@RunWith(RobolectricTestRunner.class)
public class JoinTest extends BaseTypeTest
{
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
	public void testForeignJoin() throws SQLException
	{
		createBar(createFoo("asdf", 123));
		createBar(createFoo("asdf", 123));
		createBar(createFoo("qwert", 456));
		createBar(createFoo("qwert", 456));
		createBar(createFoo("qwert", 456));

		Dao dao = helper.getDao(Bar.class);
		Where<Bar> where = new Where(dao);

		JoinAlias foo = where.join("foo");

		where.eq(foo, "ival", 456);

		Assert.assertEquals(3, dao.query(where).list().size());
	}

	@Test
	public void testSubJoin() throws SQLException
	{
		createAsdf(createBar(createFoo("asdf", 123)));
		createAsdf(createBar(createFoo("asdf", 123)));
		createAsdf(createBar(createFoo("qwert", 456)));
		createAsdf(createBar(createFoo("qwert", 456)));
		createAsdf(createBar(createFoo("qwert", 456)));

		Dao dao = helper.getDao(Asdf.class);
		Where<Asdf> where = new Where(dao);

		JoinAlias fooJoin = where.join("bar").join("foo");

		where.eq(fooJoin, "ival", 456);

		Assert.assertEquals(3, dao.query(where).list().size());
	}

	private Foo createFoo(String name, int ival) throws SQLException
	{
		Foo foo = new Foo();
		foo.name = name;
		foo.ival = ival;

		helper.getDao(Foo.class).create(foo);

		return foo;
	}

	private Bar createBar(Foo foo) throws SQLException
	{
		Bar bar = new Bar();
		bar.foo = foo;

		helper.getDao(Bar.class).create(bar);

		return bar;
	}

	private Asdf createAsdf(Bar bar) throws SQLException
	{
		Asdf asdf = new Asdf();
		asdf.bar = bar;

		helper.getDao(Asdf.class).create(asdf);

		return asdf;
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

	@DatabaseTable
	protected static class Bar
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Foo foo;
	}

	@DatabaseTable
	protected static class Asdf
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Bar bar;
	}

	private BaseTypeTest.SimpleHelper getHelper()
	{
		return createHelper(
				Asdf.class, Bar.class, Foo.class
		);
	}
}
