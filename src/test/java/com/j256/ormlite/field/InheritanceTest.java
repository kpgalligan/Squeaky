package com.j256.ormlite.field;

import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.field.types.BaseTypeTest;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

/**
 * Created by kgalligan on 8/8/15.
 */
@RunWith(RobolectricTestRunner.class)
public class InheritanceTest extends BaseTypeTest
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
		Dao<Foo, Integer> dao = helper.getDao(Foo.class);

		Foo foo = new Foo(22, 123, "456", new Date());

		dao.create(foo);

		Foo dbVal = dao.queryForAll().get(0);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(foo, dbVal));
	}

	abstract static class BaseFoo
	{
		@DatabaseField(id = true)
		public final int id;

		@DatabaseField
		public final int aval;

		@DatabaseField
		public final String bval;

		public BaseFoo(int id, int aval, String bval)
		{
			this.id = id;
			this.aval = aval;
			this.bval = bval;
		}
	}

	@DatabaseTable
	static class Foo extends BaseFoo
	{
		@DatabaseField
		public final Date cval;

		public Foo(int id, int aval, String bval, Date cval)
		{
			super(id, aval, bval);
			this.cval = cval;
		}
	}

	private SimpleHelper getHelper()
	{
		return createHelper(
				Foo.class
		);
	}
}
