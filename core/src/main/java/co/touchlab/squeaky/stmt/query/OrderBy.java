package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.stmt.ArgumentHolder;

public class OrderBy {

	private final String columnName;
	private final boolean ascending;
	private final String rawSql;
	private final ArgumentHolder[] orderByArgs;

	public OrderBy(String columnName, boolean ascending) {
		this.columnName = columnName;
		this.ascending = ascending;
		this.rawSql = null;
		this.orderByArgs = null;
	}

	public OrderBy(String rawSql, ArgumentHolder[] orderByArgs) {
		this.columnName = null;
		this.ascending = true;
		this.rawSql = rawSql;
		this.orderByArgs = orderByArgs;
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public String getRawSql() {
		return rawSql;
	}

	public ArgumentHolder[] getOrderByArgs() {
		return orderByArgs;
	}
}