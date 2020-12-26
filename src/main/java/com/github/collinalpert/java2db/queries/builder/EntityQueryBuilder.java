package com.github.collinalpert.java2db.queries.builder;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.QueryParameters;
import com.github.collinalpert.lambda2sql.Lambda2Sql;

import java.util.StringJoiner;

/**
 * Responsible for building the SQL query which expects multiple rows.
 *
 * @author Collin Alpert
 */
public class EntityQueryBuilder<E extends BaseEntity> extends QueryBuilder<E> {

	public EntityQueryBuilder(Class<E> type) {
		super(type);
	}

	/**
	 * @param queryParameters The parameters of this query. They contain the clauses.
	 * @return A {@code String} with the query clauses which modify the DQL query, like a WHERE clause, a GROUP BY clause, or an ORDER BY clause.
	 */
	@Override
	String buildQueryClauses(QueryParameters<E> queryParameters) {
		var buffer = new StringBuffer(" ");

		appendWhereClause(buffer, queryParameters.getWhereClause());

		var groupByClause = queryParameters.getGroupByClause();
		if (groupByClause != null) {
			var joiner = new StringJoiner(", ", "group by ", "");
			for (var groupBy : groupByClause) {
				joiner.add(Lambda2Sql.toSql(groupBy, super.tableName));
			}

			buffer.append(' ').append(joiner.toString());
		}

		var orderByClause = queryParameters.getOrderByClause();
		if (orderByClause != null) {
			buffer.append(' ').append(orderByClause.buildSql(super.tableName));
		}

		if (queryParameters.getLimit() != null) {
			buffer.append(" limit ").append(queryParameters.getLimitOffset()).append(", ").append(queryParameters.getLimit());
		}

		return buffer.toString();
	}
}
