package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.SqueakyContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Raw part of the where to just stick in a string in the middle of the WHERE. It is up to the user to do so properly.
 * 
 * @author graywatson
 */
public class Raw implements Clause {

	private final String statement;

	public Raw(String statement) {
		this.statement = statement;
	}

	public void appendSql(SqueakyContext squeakyContext, String tableName, StringBuilder sb) {
		sb.append(statement);
	}

	@Override
	public void appendValue(SqueakyContext squeakyContext, List<String> params) throws SQLException
	{
		//None
	}
}
