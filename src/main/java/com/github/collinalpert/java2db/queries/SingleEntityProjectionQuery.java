package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.trigersoft.jaque.expression.LambdaExpression;

import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Collin Alpert
 */
public class SingleEntityProjectionQuery<E extends BaseEntity, R> implements SingleQueryable<R> {

	protected final Class<R> returnType;
	private final SqlFunction<E, R> projection;
	private final SingleEntityQuery<E> originalQuery;

	public SingleEntityProjectionQuery(SqlFunction<E, R> projection, SingleEntityQuery<E> originalQuery) {
		var lambda = LambdaExpression.parse(projection);
		this.returnType = (Class<R>) lambda.getBody().getResultType();
		this.projection = projection;
		this.originalQuery = originalQuery;
	}

	@Override
	public Optional<R> first() {
		try (var connection = new DBConnection();
			 var result = connection.execute(getQuery())) {

			if (result.next()) {
				return Optional.ofNullable(result.getObject(1, this.returnType));
			}

			return Optional.empty();
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	@Override
	public String getQuery() {
		var builder = new StringBuilder("select ");

		var tableName = originalQuery.getTableName();
		var columnName = Lambda2Sql.toSql(projection, tableName);
		builder.append(columnName).append(" from `").append(tableName).append("`");

		builder.append(originalQuery.getQueryClauses(tableName));

		// Since we only want to fetch one result anyway.
		builder.append(" limit 1");

		return builder.toString();
	}
}
