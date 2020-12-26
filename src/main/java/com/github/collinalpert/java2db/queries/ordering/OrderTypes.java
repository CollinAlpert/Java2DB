package com.github.collinalpert.java2db.queries.ordering;

/**
 * An enum representing the sorting order possibilities in a DQL statement.
 *
 * @author Collin Alpert
 */
public enum OrderTypes {
	ASCENDING("asc"), DESCENDING("desc");

	private final String sqlKeyword;

	OrderTypes(String sqlKeyword) {
		this.sqlKeyword = sqlKeyword;
	}

	public String getSqlKeyword() {
		return sqlKeyword;
	}
}
