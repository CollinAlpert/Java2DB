package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.*;
import com.github.collinalpert.lambda2sql.functions.*;

/**
 * @author Collin Alpert
 */
public class AsyncSingleEntityQuery<E extends BaseEntity> extends SingleEntityQuery<E> implements AsyncSingleQueryable<E> {

	public AsyncSingleEntityQuery(Class<E> type) {
		super(type);
	}

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	@Override
	public AsyncSingleEntityQuery<E> where(SqlPredicate<E> predicate) {
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
	public AsyncSingleEntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		super.orWhere(predicate);
		return this;
	}

	/**
	 * Selects only a single column from a table. This is meant if you don't want to fetch an entire entity from the database.
	 *
	 * @param projection The column to project to.
	 * @param <R>        The type of the column you want to retrieve.
	 * @return A queryable containing the projection.
	 */
	@Override
	public <R> AsyncSingleQueryable<R> project(SqlFunction<E, R> projection) {
		return new AsyncSingleEntityProjectionQuery<>(projection, this);
	}
}
