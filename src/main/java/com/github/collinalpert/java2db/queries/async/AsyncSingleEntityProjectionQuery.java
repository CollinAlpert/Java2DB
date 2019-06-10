package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.SingleEntityProjectionQuery;
import com.github.collinalpert.java2db.queries.SingleEntityQuery;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

/**
 * @author Collin Alpert
 */
public class AsyncSingleEntityProjectionQuery<E extends BaseEntity, R> extends SingleEntityProjectionQuery<E, R> implements AsyncSingleQueryable<R> {

	public AsyncSingleEntityProjectionQuery(SqlFunction<E, R> projection, SingleEntityQuery<E> originalQuery) {
		super(projection, originalQuery);
	}
}
