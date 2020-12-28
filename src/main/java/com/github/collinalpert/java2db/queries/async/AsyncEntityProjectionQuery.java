package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.database.TransactionManager;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.*;
import com.github.collinalpert.java2db.queries.builder.IQueryBuilder;

/**
 * @author Collin Alpert
 */
public class AsyncEntityProjectionQuery<E extends BaseEntity, R> extends EntityProjectionQuery<E, R> implements AsyncQueryable<R> {

	public AsyncEntityProjectionQuery(Class<R> returnType, IQueryBuilder<E> queryBuilder, QueryParameters<E> queryParameters, TransactionManager transactionManager) {
		super(returnType, queryBuilder, queryParameters, transactionManager);
	}
}
