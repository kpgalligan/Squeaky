package co.touchlab.squeaky.dao;

import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kgalligan on 9/14/15.
 */
@RunWith(RobolectricTestRunner.class)
public class DaoTest extends BaseTypeTest
{
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
	public void testQueryForId() throws SQLException
	{
		Foo foo = createFoo("asdf", 123, 23523534234l, new Date());
		Assert.assertEquals(getDao().queryForId(foo.id).lval, foo.lval);
	}



	@Test
	public void testQueryForAll() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		Assert.assertEquals(getDao().queryForAll().size(), 3);
	}


	@Test
	public void testQueryForEq() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 444, 23523534255l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		Assert.assertEquals(getDao().queryForEq(
				DaoTest$Foo$$Configuration.Fields.ival.name(),
				123
		).size(), 2);

		Assert.assertEquals(getDao().queryForEq(
				DaoTest$Foo$$Configuration.Fields.ival.name(),
				444).get(0).lval, 23523534255l);
	}

	@Test
	public void testQueryForFieldValues() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 444, 23523534255l, new Date());
		createFoo("asdf", 123, 23523534255l, new Date());
		Map<String, Object> fieldValues = new HashMap<>();
		fieldValues.put("name", "asdf");
		fieldValues.put("ival", 444);
		Assert.assertEquals(getDao().queryForFieldValues(
				fieldValues
		).size(), 1);

		fieldValues.remove("ival");
		fieldValues.put("lval", 23523534255l);

		Assert.assertEquals(getDao().queryForFieldValues(
				fieldValues
		).size(), 2);

	}

	private Foo createFoo(String name, int ival, long lval, Date aDate) throws SQLException
	{
		Foo foo = new Foo();
		foo.name = name;
		foo.ival = ival;
		foo.lval = lval;
		foo.sDate = aDate;

		getDao().create(foo);

		return foo;
	}

	private Dao<Foo, Integer> getDao()
	{
		return helper.getDao(Foo.class);
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

		@DatabaseField(dataType = DataType.DATE_STRING, format = "MM/dd/yyyy HH:mm")
		Date sDate;
	}

	private BaseTypeTest.SimpleHelper getHelper()
	{
		return createHelper(
				Foo.class
		);
	}
}
