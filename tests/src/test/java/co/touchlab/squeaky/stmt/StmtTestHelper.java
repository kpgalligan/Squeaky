package co.touchlab.squeaky.stmt;

import co.touchlab.squeaky.stmt.query.Queryable;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

/**
 * Created by kgalligan on 9/8/15.
 */
public class StmtTestHelper
{
	public static void assertWhere(String wherePart, Where w, String[] params)throws Exception
	{
		String whereStatement = w.getStatement();
		Statement l = CCJSqlParserUtil.parse("SELECT * FROM foo where " + whereStatement);
		Statement r = CCJSqlParserUtil.parse("SELECT * FROM foo WHERE " + wherePart);
		Assert.assertTrue(StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(l.toString()), StringUtils.trimToEmpty(r.toString())));

		String[] whereParams = w.getParameters();
		if(whereParams != null || params != null)
		{
			Assert.assertEquals(whereParams.length, params.length);
			for (int i=0; i<whereParams.length; i++)
			{
				Assert.assertEquals(whereParams[i], params[i]);
			}
		}
	}
}
