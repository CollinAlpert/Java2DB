package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.EntityProjectionQuery;
import com.github.collinalpert.java2db.queries.EntityQuery;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

/**
 * @author Collin Alpert
 */
public class AsyncEntityProjectionQuery<E extends BaseEntity, R> extends EntityProjectionQuery<E, R> implements AsyncQueryable<R> {

	public AsyncEntityProjectionQuery(SqlFunction<E, R> projection, EntityQuery<E> originalQuery) {
		super(projection, originalQuery);
	}
}
