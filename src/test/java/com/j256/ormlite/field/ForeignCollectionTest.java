package com.j256.ormlite.field;

import com.j256.ormlite.android.apptools.BaseTest;
import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.field.types.BaseTypeTest;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.assertTrue;

/**
 * Created by kgalligan on 7/26/15.
 */
@RunWith(RobolectricTestRunner.class)
public class ForeignCollectionTest extends BaseTypeTest
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
		/*Dao<Parent, Integer> parentDao = helper.getDao(Parent.class);

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

		Parent parentDb = parentDao.queryForAll().get(0);
		parentDao.fillForeignCollection(parentDb, "children");

		assertTrue(parentDb.children.equals(children));*/
	}

	@DatabaseTable
	protected static class Parent
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@ForeignCollectionField(foreignFieldName = "parent")
		List<Child> children;
	}

	@DatabaseTable
	protected static class Child {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		Parent parent;

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Child child = (Child) o;

			if (id != child.id) return false;
			if (asdf != null ? !asdf.equals(child.asdf) : child.asdf != null) return false;
			return !(parent != null ? !(parent.id == child.parent.id) : child.parent != null);

		}

		@Override
		public int hashCode()
		{
			int result = id;
			result = 31 * result + (asdf != null ? asdf.hashCode() : 0);
			result = 31 * result + (parent != null ? parent.hashCode() : 0);
			return result;
		}
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Child.class,
				Parent.class
		);
	}
}
