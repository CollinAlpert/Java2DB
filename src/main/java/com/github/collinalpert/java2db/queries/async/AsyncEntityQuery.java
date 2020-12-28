package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.expressions.expression.LambdaExpression;
import com.github.collinalpert.java2db.database.TransactionManager;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.EntityQuery;
import com.github.collinalpert.java2db.queries.builder.*;
import com.github.collinalpert.java2db.queries.ordering.OrderTypes;
import com.github.collinalpert.java2db.services.BaseService;
import com.github.collinalpert.lambda2sql.functions.*;

import java.util.List;

/**
 * @author Collin Alpert
 */
public class AsyncEntityQuery<E extends BaseEntity> extends EntityQuery<E> implements AsyncQueryable<E> {
	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link BaseService}.
	 *
	 * @param type The entity to query.
	 */
	public AsyncEntityQuery(Class<E> type, TransactionManager transactionManager) {
		super(type, transactionManager);
	}

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	@Override
	public AsyncEntityQuery<E> where(SqlPredicate<E> predicate) {
		super.where(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	@Override
	public AsyncEntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		super.orWhere(predicate);
		return this;
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(SqlFunction<E, ?> function) {
		super.orderBy(function, OrderTypes.ASCENDING);
		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(SqlFunction<E, ?>[] functions) {
		super.orderBy(functions, OrderTypes.ASCENDING);
		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(List<SqlFunction<E, ?>> functions) {
		super.orderBy(functions, OrderTypes.ASCENDING);
		return this;
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement with a sorting order option.
	 *
	 * @param function  The column to order by.
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(SqlFunction<E, ?> function, OrderTypes orderType) {
		super.orderBy(function, orderType);

		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement with a sorting order option. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(SqlFunction<E, ?>[] functions, OrderTypes orderType) {
		super.orderBy(functions, orderType);

		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement with a sorting order option. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@Override
	public AsyncEntityQuery<E> orderBy(List<SqlFunction<E, ?>> functions, OrderTypes orderType) {
		super.orderBy(functions, orderType);
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer with an offset.
	 * For example, the call <code>.limit(10, 5)</code> would return the rows 6-15.
	 *
	 * @param limit  The maximum of rows to be returned.
	 * @param offset The offset of the limit.
	 * @return This {@link EntityQuery} object, now with a LIMIT with an OFFSET.
	 */
	public AsyncEntityQuery<E> limit(int limit, int offset) {
		super.limit(limit, offset);
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link EntityQuery} object, now with a LIMIT.
	 */
	public AsyncEntityQuery<E> limit(int limit) {
		super.limit(limit);
		return this;
	}

	/**
	 * Selects only a single column from a table. This is meant if you don't want to fetch an entire entity from the database.
	 *
	 * @param projection The column to project to.
	 * @param <R>        The type of the column you want to retrieve.
	 * @return A queryable containing the projection.
	 */
	public <R> AsyncQueryable<R> project(SqlFunction<E, R> projection) {
		@SuppressWarnings("unchecked") var returnType = (Class<R>) LambdaExpression.parse(projection).getBody().getResultType();
		var queryBuilder = new ProjectionQueryBuilder<>(projection, super.getTableName(), (QueryBuilder<E>) super.queryBuilder);

		return new AsyncEntityProjectionQuery<>(returnType, queryBuilder, super.queryParameters, super.transactionManager);
	}
}
