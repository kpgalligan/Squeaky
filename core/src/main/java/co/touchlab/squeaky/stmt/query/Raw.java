package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.stmt.ArgumentHolder;

import java.util.List;

/**
 * Raw part of the where to just stick in a string in the middle of the WHERE. It is up to the user to do so properly.
 * 
 * @author graywatson
 */
public class Raw implements Clause {

	private final String statement;
	private final ArgumentHolder[] args;

	public Raw(String statement, ArgumentHolder[] args) {
		this.statement = statement;
		this.args = args;
	}

	public void appendSql(String tableName, StringBuilder sb, List<ArgumentHolder> argList) {
		sb.append(statement);
		sb.append(' ');
		for (ArgumentHolder arg : args) {
			argList.add(arg);
		}
	}
}
