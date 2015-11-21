package co.touchlab.squeaky.field;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.DaoHelper;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.DatabaseTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kgalligan on 7/26/15.
 */
@RunWith(RobolectricTestRunner.class)
public class ForeignFieldRefreshTest extends BaseTypeTest
{
	public static final String PREFIX = "Hello ";
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
	public void testRefreshMapAutofill() throws SQLException
	{
		Dao.ForeignRefresh[] oneLevel = DaoHelper.fillForeignRefreshMap(helper.getSqueakyContext(), ChildEager.class, 1);

		Dao.ForeignRefresh[] oneLevelCompare = DaoHelper.refresh("parent");

		Assert.assertTrue(sameRefreshMap(oneLevel, oneLevelCompare));

		Dao.ForeignRefresh[] twoLevel = DaoHelper.fillForeignRefreshMap(helper.getSqueakyContext(), ChildEager.class, 2);

		Dao.ForeignRefresh[] twoLevelCompare = DaoHelper.refresh("parent[grandParentEager]");

		Assert.assertTrue(sameRefreshMap(twoLevel, twoLevelCompare));

		Dao.ForeignRefresh[] threeLevel = DaoHelper.fillForeignRefreshMap(helper.getSqueakyContext(), ChildEager.class, 3);

		Dao.ForeignRefresh[] threeLevelCompare = DaoHelper.refresh("parent[grandParentEager[childEager]]");

		Assert.assertTrue(sameRefreshMap(threeLevel, threeLevelCompare));

		//Inception time
		Dao.ForeignRefresh[] sixLevel = DaoHelper.fillForeignRefreshMap(helper.getSqueakyContext(), ChildEager.class, 6);

		Dao.ForeignRefresh[] sixLevelCompare = DaoHelper.refresh("parent[grandParentEager[childEager[" +
				"parent[grandParentEager[childEager]]" +
				"]]]");

		Assert.assertTrue(sameRefreshMap(sixLevel, sixLevelCompare));

		Dao.ForeignRefresh[] nineLevel = DaoHelper.fillForeignRefreshMap(helper.getSqueakyContext(), ChildEager.class, 9);

		Dao.ForeignRefresh[] nineLevelCompare = DaoHelper.refresh(
				"parent[grandParentEager[childEager[" +
					"parent[grandParentEager[childEager[" +
						"parent[grandParentEager[childEager]]" +
					"]]]"+
				"]]]");

		Assert.assertTrue(sameRefreshMap(nineLevel, nineLevelCompare));
	}

	@Test
	public void testForeignRefreshMap() throws SQLException
	{
		Dao<GrandParent> grandParentDao = helper.getDao(GrandParent.class);
		Dao<Parent> parentDao = helper.getDao(Parent.class);
		Dao<ChildEager> childEagerDao = helper.getDao(ChildEager.class);

		GrandParent grampsEager = new GrandParent();
		grampsEager.name = "grampsEager";
		GrandParent grampsLazy = new GrandParent();
		grampsLazy.name = "grampsLazy";
		grandParentDao.create(grampsEager);
		grandParentDao.create(grampsLazy);

		Parent popsEager = new Parent();
		popsEager.name = "popsEager";
		Parent popsLazy = new Parent();
		popsLazy.name = "popsLazy";
		popsEager.grandParentEager = grampsEager;
		popsEager.grandParentLazy = grampsLazy;
		popsLazy.grandParentEager = grampsEager;
		popsLazy.grandParentLazy = grampsLazy;

		parentDao.create(popsEager);
		parentDao.create(popsLazy);

		ChildEager child = new ChildEager();
		child.asdf = "child";
		child.parent = popsEager;
		childEagerDao.create(child);

		grampsEager.childEager = child;
		grampsLazy.childEager = child;
		grandParentDao.update(grampsEager);
		grandParentDao.update(grampsLazy);

		ChildEager testDefault = childEagerDao.queryForId(child.id);
		Assert.assertTrue(testDefault.parent.grandParentLazy.name == null && testDefault.parent.grandParentEager.name != null && testDefault.parent.grandParentEager.childEager.asdf == null);

		//Disable all refresh.  Kind of garbage syntax.  To review.
		ChildEager noRefresh = childEagerDao.query(new Where<ChildEager>(childEagerDao).eq("id", child.id)).foreignRefreshMap(new Dao.ForeignRefresh[0]).list().get(0);
		Assert.assertTrue(noRefresh.parent.name == null);

		ChildEager deepRefresh = childEagerDao.query(new Where<ChildEager>(childEagerDao).eq("id", child.id)).foreignRefreshMap(
				DaoHelper.refresh("parent[grandParentLazy[childEager]]")
		).list().get(0);

		Assert.assertTrue(deepRefresh.parent.name != null && deepRefresh.parent.grandParentEager.name == null &&
				deepRefresh.parent.grandParentLazy.childEager.asdf != null);

		Parent testParent = parentDao.query(new Where<ChildEager>(parentDao).eq("id", popsEager.id))
				.foreignRefreshMap(DaoHelper.refresh("grandParentLazy[childEager[parent[grandParentEager]]],grandParentEager"))
				.list().get(0);

		Assert.assertTrue(
				testParent.grandParentLazy.childEager.parent.grandParentEager.name != null &&
						testParent.grandParentLazy.childEager.parent.grandParentEager.childEager.asdf == null &&
						testParent.grandParentEager.childEager.asdf == null
		);
	}

	private boolean sameRefreshMap(Dao.ForeignRefresh[] left, Dao.ForeignRefresh[] right)
	{
		if(left == null && right == null)
			return true;

		if(left == null || right == null)
			return false;

		if(left.length != right.length)
			return false;

		for (Dao.ForeignRefresh leftRefresh : left)
		{
			boolean fieldEquals = false;

			for (Dao.ForeignRefresh rightRefresh : right)
			{
				if(leftRefresh.field.equals(rightRefresh.field))
				{
					fieldEquals = sameRefreshMap(leftRefresh.refreshFields, rightRefresh.refreshFields);
					break;
				}
			}

			if(!fieldEquals)
				return false;
		}

		return true;
	}

	@DatabaseTable
	static class GrandParent
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		ChildEager childEager;
	}

	@DatabaseTable
	protected static class Parent
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String name;

		@DatabaseField(foreign = true)
		GrandParent grandParentLazy;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		GrandParent grandParentEager;
	}

	@DatabaseTable
	protected static class Child
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true)
		Parent parentLazy;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Parent parentEager;


	}

	@DatabaseTable
	protected static class ChildEager
	{
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String asdf;

		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Parent parent;

	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Child.class,
				ChildEager.class,
				Parent.class,
				GrandParent.class
		);
	}
}
