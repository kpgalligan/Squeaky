package co.touchlab.squeaky.dao;

import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
		Assert.assertEquals(getFooDao().queryForId(foo.id).lval, foo.lval);
	}

	@Test
	public void testQueryForAll() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		Assert.assertEquals(getFooDao().queryForAll().list().size(), 3);
	}

	@Test
	public void testQueryForEq() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 444, 23523534255l, new Date());
		createFoo("asdf", 123, 23523534234l, new Date());
		Assert.assertEquals(getFooDao().queryForEq(
				DaoTest$Foo$$Configuration.Fields.ival.name(),
				123
		).list().size(), 2);

		Assert.assertEquals(getFooDao().queryForEq(
				DaoTest$Foo$$Configuration.Fields.ival.name(),
				444).list().get(0).lval, 23523534255l);
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
		Assert.assertEquals(getFooDao().queryForFieldValues(
				fieldValues
		).list().size(), 1);

		fieldValues.remove("ival");
		fieldValues.put("lval", 23523534255l);

		Assert.assertEquals(getFooDao().queryForFieldValues(
				fieldValues
		).list().size(), 2);

	}

	@Test
	public void testQueryObject() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 444, 23523534255l, new Date());
		createFoo("asdf", 123, 23523534255l, new Date());
		List<Foo> results = getFooDao().query(new Query()
		{
			@Override
			public String getFromStatement(boolean joinsAllowed) throws SQLException
			{
				return "foo t";
			}

			@Override
			public String getWhereStatement(boolean joinsAllowed) throws SQLException
			{
				return "t.ival = ?";
			}

			@Override
			public String[] getParameters() throws SQLException
			{
				return new String[]{"123"};
			}
		}).list();

		Assert.assertEquals(results.size(), 2);
	}

	@Test(expected = SQLException.class)
	public void testCreateIfNotExistsFail() throws SQLException
	{
		createBar(12, "qwert", false);
		createBar(12, "qwert", false);
		Assert.assertEquals(1, getBarDao().countOf());
	}

	@Test
	public void testCreateIfNotExistsSkip() throws SQLException
	{
		createBar(12, "qwert", true);
		createBar(12, "qwert", true);
		Assert.assertEquals(1, getBarDao().countOf());
	}

	@Test
	public void testCreateOrUpdate() throws SQLException
	{
		getBarDao().createOrUpdate(new Bar(13, "aaa"));
		Assert.assertEquals(getBarDao().queryForEq("name", "aaa").list().size(), 1);
		getBarDao().createOrUpdate(new Bar(13, "bbb"));
		Assert.assertEquals(getBarDao().queryForEq("name", "aaa").list().size(), 0);
		Assert.assertEquals(getBarDao().queryForEq("name", "bbb").list().size(), 1);
	}

	@Test
	public void testUpdate() throws SQLException
	{
		getBarDao().create(new Bar(13, "aaa"));
		Assert.assertEquals(getBarDao().queryForEq("name", "aaa").list().size(), 1);
		getBarDao().update(new Bar(13, "bbb"));
		Assert.assertEquals(getBarDao().queryForEq("name", "aaa").list().size(), 0);
		Assert.assertEquals(getBarDao().queryForEq("name", "bbb").list().size(), 1);
	}

	@Test
	public void testUpdateId() throws SQLException
	{
		Bar bar = new Bar(13, "aaa");
		getBarDao().create(bar);
		Assert.assertEquals(getBarDao().queryForEq("id", "13").list().size(), 1);
		getBarDao().updateId(bar, 14);
		Assert.assertEquals(getBarDao().queryForEq("id", "13").list().size(), 0);
		Assert.assertEquals(getBarDao().queryForEq("id", "14").list().size(), 1);
	}

	@Test
	public void testUpdateQuery() throws SQLException
	{
		createFoo("asdf", 123, 23523534234l, new Date());
		createFoo("asdf", 444, 23523534255l, new Date());
		createFoo("asdf", 123, 23523534255l, new Date());

		Dao<Foo, Integer> fooDao = getFooDao();
		Where<Foo, Integer> where = new Where<>(fooDao);
		where.eq("ival", 123);

		Assert.assertEquals(2, fooDao.countOf(where));

		Map<String, Object> changes = new HashMap<>();
		changes.put("ival", 444);
		fooDao.update(where, changes);
		Assert.assertEquals(0, fooDao.countOf(where));
		Assert.assertEquals(3, fooDao.countOf(where.reset().eq("ival", 444)));
	}

	@Test
	public void testRefresh() throws SQLException
	{
		Foo foo = createFoo("asdf", 123, 23523534234l, new Date());
		Foo dbFoo = getFooDao().queryForAll().list().get(0);
		dbFoo.ival = 222;
		getFooDao().update(dbFoo);

		getFooDao().refresh(foo);
		Assert.assertEquals(222, foo.ival);
	}

	@Test
	public void testDelete() throws SQLException
	{
		Bar bar = createBar(123, "gobyebye", false);
		Assert.assertEquals(1, getBarDao().countOf());
		getBarDao().delete(bar);
		Assert.assertEquals(0, getBarDao().countOf());
	}

	@Test
	public void testDeleteById() throws SQLException
	{
		Bar bar = createBar(123, "gobyebye", false);
		Assert.assertEquals(1, getBarDao().countOf());
		getBarDao().deleteById(123);
		Assert.assertEquals(0, getBarDao().countOf());
	}

	@Test
	public void testDeleteCollection() throws SQLException
	{
		List<Bar> bars = new ArrayList<>();
		bars.add(createBar(12, "a", false));
		bars.add(createBar(13, "b", false));
		bars.add(createBar(14, "c", false));

		Assert.assertEquals(3, getBarDao().countOf());

		getBarDao().delete(bars);

		Assert.assertEquals(0, getBarDao().countOf());
	}

	@Test
	public void testDeleteQuery() throws SQLException
	{
		createBar(12, "a", false);
		createBar(13, "b", false);
		createBar(14, "c", false);

		Dao<Bar, Integer> dao = getBarDao();
		Where<Bar, Integer> where = new Where<Bar, Integer>(dao);
		where.ge("id", 13);

		dao.delete(where);

		Assert.assertEquals(1, dao.countOf());
	}

	@Test
	public void testIterator() throws SQLException
	{
		createBar(12, "a", false);
		createBar(13, "b", false);
		createBar(14, "c", false);

		//Not sure we can depend on sqlite order, but this passes
		CloseableIterator<Bar> iterator = getBarDao().iterator();
		Bar bar12 = iterator.next();
		Assert.assertEquals(12, bar12.id);
		Bar bar13 = iterator.next();
		Assert.assertEquals(13, bar13.id);
		Bar bar14 = iterator.next();
		Assert.assertEquals(14, bar14.id);

		iterator.closeQuietly();
	}

	@Test
	public void testIteratorQuery() throws SQLException
	{
		createBar(12, "a", false);
		createBar(13, "b", false);
		createBar(14, "c", false);

		//Not sure we can depend on sqlite order, but this passes
		Where<Bar, Integer> where = new Where<Bar, Integer>(getBarDao());
		where.in("id", 12, 14);

		CloseableIterator<Bar> iterator = getBarDao().iterator(where);
		Bar bar12 = iterator.next();
		Assert.assertEquals(12, bar12.id);
		Bar bar14 = iterator.next();
		Assert.assertEquals(14, bar14.id);

		Assert.assertFalse(iterator.hasNext());

		iterator.closeQuietly();
	}

	@Test
	public void testQueryRawValue() throws SQLException
	{
		createBar(12, "a", false);
		createBar(13, "a", false);
		createBar(14, "c", false);
		createBar(15, "c", false);
		createBar(16, "c", false);

		long sum = getBarDao().queryRawValue("select sum(id) from bar where name = ?", new String[]{"a"});
		Assert.assertEquals(25, sum);
	}

	@Test
	public void testCountOf() throws SQLException
	{
		createBar(12, "a", false);
		createBar(13, "a", false);
		createBar(14, "c", false);
		createBar(15, "c", false);
		createBar(16, "c", false);

		Assert.assertEquals(5, getBarDao().countOf());
		Where<Bar, Integer> where = new Where<Bar, Integer>(getBarDao());
		where.eq("name", "c");
		Assert.assertEquals(3, getBarDao().countOf(where));
	}

	@Test
	public void testObserver() throws SQLException
	{
		final AtomicInteger changeCount = new AtomicInteger();
		Dao.DaoObserver observer = new Dao.DaoObserver()
		{
			@Override
			public void onChange()
			{
				changeCount.addAndGet(1);
			}
		};
		getBarDao().registerObserver(observer);

		getBarDao().create(new Bar(12, "asdf"));
		getBarDao().create(new Bar(13, "asdf"));
		getBarDao().update(new Bar(13, "qwert"));

		Assert.assertEquals(3, changeCount.get());

		getBarDao().unregisterObserver(observer);
		getBarDao().create(new Bar(14, "asdf"));
		getBarDao().create(new Bar(15, "asdf"));
		getBarDao().update(new Bar(15, "qwert"));

		Assert.assertEquals(3, changeCount.get());
	}

	private Foo createFoo(String name, int ival, long lval, Date aDate) throws SQLException
	{
		Foo foo = new Foo();
		foo.name = name;
		foo.ival = ival;
		foo.lval = lval;
		foo.sDate = aDate;

		getFooDao().create(foo);

		return foo;
	}

	private Bar createBar(int id, String name, boolean ifNotExists) throws SQLException
	{
		Bar bar = new Bar(id, name);

		if (ifNotExists)
			getBarDao().createIfNotExists(bar);
		else
			getBarDao().create(bar);

		return bar;
	}

	private Dao<Foo, Integer> getFooDao()
	{
		return helper.getDao(Foo.class);
	}

	private Dao<Bar, Integer> getBarDao()
	{
		return helper.getDao(Bar.class);
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

	@DatabaseTable
	protected static class Bar
	{
		public Bar(int id, String name)
		{
			this.id = id;
			this.name = name;
		}

		@DatabaseField(id = true)
		final int id;

		@DatabaseField
		final String name;
	}

	private BaseTypeTest.SimpleHelper getHelper()
	{
		return createHelper(
				Foo.class, Bar.class
		);
	}
}
