package com.github.collinalpert.java2db.queries.builder;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.QueryParameters;

/**
 * @author Collin Alpert
 */
public class SingleEntityQueryBuilder<E extends BaseEntity> extends QueryBuilder<E> {

	public SingleEntityQueryBuilder(Class<E> type) {
		super(type);
	}

	@Override
	String buildQueryClauses(QueryParameters<E> queryParameters) {
		var buffer = new StringBuffer();

		appendWhereClause(buffer, queryParameters.getWhereClause());

		// Since we only want to fetch one result anyway.
		buffer.append(" limit 1");

		return buffer.toString();
	}
}
