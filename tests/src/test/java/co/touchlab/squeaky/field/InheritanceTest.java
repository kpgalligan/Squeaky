package co.touchlab.squeaky.field;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.types.BaseTypeTest;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
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

	@After
	public void after()
	{
		helper.close();
	}

	@Test
	public void testForeignCollectionEager() throws Exception
	{
		Dao<Foo> dao = helper.getDao(Foo.class);

		Foo foo = new Foo(22, 123, "456", new Date());

		dao.create(foo);

		Foo dbVal = dao.queryForAll().list().get(0);
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
