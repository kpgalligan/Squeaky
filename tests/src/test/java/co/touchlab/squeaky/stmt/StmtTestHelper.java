package co.touchlab.squeaky.stmt;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Assert;

/**
 * Created by kgalligan on 9/8/15.
 */
public class StmtTestHelper
{
	public static void assertWhere(String wherePart, Where w)throws Exception
	{
		String whereStatement = w.getStatement();
		Statement l = CCJSqlParserUtil.parse("SELECT * FROM foo where " + whereStatement);
		Statement r = CCJSqlParserUtil.parse("SELECT * FROM foo WHERE " + wherePart);
		Assert.assertEquals(l.toString(), r.toString());
	}
}
