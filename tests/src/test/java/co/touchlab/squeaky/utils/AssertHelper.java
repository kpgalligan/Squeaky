package co.touchlab.squeaky.utils;

import junit.framework.Assert;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by kgalligan on 9/19/15.
 */
public class AssertHelper
{
	public static void assertEquals(Collection first, Collection second)
	{
		Iterator firstIter = first.iterator();
		Iterator secondIter = second.iterator();
		while (firstIter.hasNext())
		{
			Object a = firstIter.next();
			Object b = secondIter.next();

			Assert.assertEquals(a, b);
		}

		Assert.assertFalse(secondIter.hasNext());
	}
}
