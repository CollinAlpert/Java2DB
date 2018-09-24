package com.github.collinalpert.java2db.queries;

/**
 * An enum representing the sorting order possibilities in a DQL statement.
 *
 * @author Collin Alpert
 */
public enum OrderTypes {
	ASCENDING("asc"), DESCENDING("desc");

	private final String sql;

	OrderTypes(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
