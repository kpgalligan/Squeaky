package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

/**
 * For operations with a number of them in a row.
 * 
 * @author graywatson
 */
public class ManyClause implements Clause, NeedsFutureClause {

	public static final String AND_OPERATION = "AND";
	public static final String OR_OPERATION = "OR";

	private final Clause first;
	private Clause second;
	private final Clause[] others;
	private final int startOthersAt;
	private final String operation;

	public ManyClause(Clause first, String operation) {
		this.first = first;
		// second will be set later
		this.second = null;
		this.others = null;
		this.startOthersAt = 0;
		this.operation = operation;
	}

	public ManyClause(Clause first, Clause second, Clause[] others, String operation) {
		this.first = first;
		this.second = second;
		this.others = others;
		this.startOthersAt = 0;
		this.operation = operation;
	}

	public ManyClause(Clause[] others, String operation) {
		this.first = others[0];
		if (others.length < 2) {
			this.second = null;
			this.startOthersAt = others.length;
		} else {
			this.second = others[1];
			this.startOthersAt = 2;
		}
		this.others = others;
		this.operation = operation;
	}

	public void appendSql(String tableName, StringBuilder sb,
			List<ArgumentHolder> selectArgList) throws SQLException {
		sb.append("(");
		first.appendSql(tableName, sb, selectArgList);
		if (second != null) {
			sb.append(operation);
			sb.append(' ');
			second.appendSql(tableName, sb, selectArgList);
		}
		if (others != null) {
			for (int i = startOthersAt; i < others.length; i++) {
				sb.append(operation);
				sb.append(' ');
				others[i].appendSql(tableName, sb, selectArgList);
			}
		}
		sb.append(") ");
	}

	public void setMissingClause(Clause right) {
		second = right;
	}
}
