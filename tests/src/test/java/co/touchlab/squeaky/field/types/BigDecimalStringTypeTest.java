package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.field.SqlType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BigDecimalStringTypeTest
{

	@Test
	public void testCoverage()
	{
		new BigDecimalStringType(SqlType.STRING, new Class[0]);
	}
}
