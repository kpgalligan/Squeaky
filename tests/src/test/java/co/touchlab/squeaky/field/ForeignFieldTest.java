package co.touchlab.squeaky.field;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by kgalligan on 7/26/15.
 */
@RunWith(RobolectricTestRunner.class)
public class ForeignFieldTest extends BaseTypeTest
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
	public void testForeignCollection() throws Exception
	{
		Dao<Parent, Integer> parentDao = helper.getDao(Parent.class);

		Parent parent = new Parent();
		parent.name = "test";
		parentDao.create(parent);

		Dao<Child,Integer> childDao = helper.getDao(Child.class);
		Random random = new Random();
		List<Child> children = new ArrayList<Child>();

		for(int i=0; i<20; i++)
		{
			Child child = new Child();
			child.asdf = "Hello "+ random.nextInt(10000);
			child.parent = parent;
			childDao.create(child);
			children.add(child);
		}

		List<String> statements = new ArrayList<>();

		{
			Where<Child, Integer> where = new Where<>(childDao);
			Where<Child, Integer> subwhere = where.eq("parent", parent);
			statements.add(subwhere.getWhereStatement());
			List<Child> childList = childDao.query(subwhere);
			assertEquals(childList.size(), 20);
		}

		{
			Where<Child, Integer> where = new Where<>(childDao);
			Where<Child, Integer> subwhere = where.eq("parent_id", parent.id);
			statements.add(subwhere.getWhereStatement());
			List<Child> childList = childDao.query(subwhere);
			assertEquals(childList.size(), 20);
		}

		{
			Where<Child, Integer> where = new Where<>(childDao);
			Where<Child, Integer> subwhere = where.eq("parent_id", parent);
			statements.add(subwhere.getWhereStatement());
			List<Child> childList = childDao.query(subwhere);
			assertEquals(childList.size(), 20);
		}

		{
			Where<Child, Integer> where = new Where<>(childDao);
			Where<Child, Integer> subwhere = where.eq("parent", parent.id);
			statements.add(subwhere.getWhereStatement());
			List<Child> childList = childDao.query(subwhere);
			assertEquals(childList.size(), 20);
		}

		String check = null;
		for (String statement : statements)
		{
			if(check == null)
			{
				check = statement == null ? "whoops" : statement;
			}
			else
			{
				assertEquals(check, statement);
			}
		}
	}

	@DatabaseTable
	protected static class Parent
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;
	}

	@DatabaseTable
	protected static class Child {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		Parent parent;
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Child.class,
				Parent.class
		);
	}
}
