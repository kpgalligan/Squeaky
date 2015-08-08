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
	public void testForeignCollectionEager() throws Exception
	{
		Dao<ParentEager, Integer> parentDao = helper.getDao(ParentEager.class);

		ParentEager parent = new ParentEager();
		parent.name = "test";
		parentDao.create(parent);

		Dao<ChildEager,Integer> childDao = helper.getDao(ChildEager.class);
		Random random = new Random();
		List<ChildEager> children = new ArrayList<ChildEager>();

		for(int i=0; i<20; i++)
		{
			ChildEager child = new ChildEager();
			child.asdf = "Hello "+ random.nextInt(10000);
			child.parent = parent;
			childDao.create(child);
			children.add(child);
		}

		ParentEager parentDb = parentDao.queryForAll().get(0);

		assertTrue(parentDb.children.equals(children));
	}

	@Test
	public void testForeignCollectionLazy() throws Exception
	{
		Dao<ParentLazy, Integer> parentDao = helper.getDao(ParentLazy.class);

		ParentLazy parent = new ParentLazy();
		parent.name = "test";
		parentDao.create(parent);

		Dao<ChildLazy,Integer> childDao = helper.getDao(ChildLazy.class);
		Random random = new Random();
		List<ChildLazy> children = new ArrayList<ChildLazy>();

		for(int i=0; i<20; i++)
		{
			ChildLazy child = new ChildLazy();
			child.asdf = "Hello "+ random.nextInt(10000);
			child.parent = parent;
			childDao.create(child);
			children.add(child);
		}

		ParentLazy parentDb = parentDao.queryForAll().get(0);
		parentDao.fillForeignCollection(parentDb, "children");

		assertTrue(parentDb.children.equals(children));
	}

	@Test
	public void testForeignCollectionString() throws Exception
	{
		Dao<ParentString, Integer> parentDao = helper.getDao(ParentString.class);

		ParentString parent = new ParentString();
		parent.id = "parentid";
		parent.name = "test";
		parentDao.create(parent);

		Dao<ChildString,Integer> childDao = helper.getDao(ChildString.class);
		Random random = new Random();
		List<ChildString> children = new ArrayList<ChildString>();

		for(int i=0; i<20; i++)
		{
			ChildString child = new ChildString();
			child.id = "childid_"+ i;
			child.asdf = "Hello "+ random.nextInt(10000);
			child.parent = parent;
			childDao.create(child);
			children.add(child);
		}

		ParentString parentDb = parentDao.queryForAll().get(0);
//		parentDao.fillForeignCollection(parentDb, "children");

		assertTrue(parentDb.children.equals(children));
	}

	@DatabaseTable
	protected static class ParentEager
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@ForeignCollectionField(foreignFieldName = "parent", eager = true)
		List<ChildEager> children;
	}

	@DatabaseTable
	protected static class ChildEager {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		ParentEager parent;

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ChildEager child = (ChildEager) o;

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

	@DatabaseTable
	protected static class ParentLazy
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@ForeignCollectionField(foreignFieldName = "parent", eager = false)
		List<ChildLazy> children;
	}

	@DatabaseTable
	protected static class ChildLazy {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		ParentLazy parent;

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ChildLazy child = (ChildLazy) o;

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

	@DatabaseTable
	protected static class ParentString
	{
		@DatabaseField(id = true)
		String id;

		@DatabaseField
		String name;

		@ForeignCollectionField(foreignFieldName = "parent", eager = true)
		List<ChildString> children;
	}

	@DatabaseTable
	protected static class ChildString {
		@DatabaseField(id = true)
		String id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		ParentString parent;

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ChildString that = (ChildString) o;

			if (id != null ? !id.equals(that.id) : that.id != null) return false;
			if (asdf != null ? !asdf.equals(that.asdf) : that.asdf != null) return false;
			return !(parent != null ? !(parent.id.equals(that.parent.id)) : that.parent != null);

		}

		@Override
		public int hashCode()
		{
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (asdf != null ? asdf.hashCode() : 0);
			result = 31 * result + (parent != null ? parent.hashCode() : 0);
			return result;
		}
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				ChildEager.class,
				ParentEager.class,
				ChildLazy.class,
				ParentLazy.class,
				ChildString.class,
				ParentString.class
		);
	}
}
