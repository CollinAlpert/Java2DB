package com.github.collinalpert.java2db.queries.builder;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.QueryParameters;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

/**
 * @author Collin Alpert
 */
public class ProjectionQueryBuilder<E extends BaseEntity, R> implements IQueryBuilder<E> {

	private final SqlFunction<E, R> projection;
	private final String tableName;
	private final QueryBuilder<E> originalBuilder;

	public ProjectionQueryBuilder(SqlFunction<E, R> projection, String tableName, QueryBuilder<E> originalBuilder) {
		this.projection = projection;
		this.tableName = tableName;
		this.originalBuilder = originalBuilder;
	}

	@Override
	public String build(QueryParameters<E> queryParameters) {
		var builder = new StringBuilder("select");

		if (queryParameters.getDistinct()) {
			builder.append(' ').append("distinct");
		}

		var columnName = Lambda2Sql.toSql(this.projection, this.tableName);
		builder.append(' ').append(columnName).append(" from `").append(this.tableName).append("`");

		builder.append(this.originalBuilder.buildQueryClauses(queryParameters));

		return builder.toString();
	}
}
