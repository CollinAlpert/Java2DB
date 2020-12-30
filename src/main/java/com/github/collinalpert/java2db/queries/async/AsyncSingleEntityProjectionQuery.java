package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.database.TransactionManager;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.*;
import com.github.collinalpert.java2db.queries.builder.ProjectionQueryBuilder;

/**
 * @author Collin Alpert
 */
public class AsyncSingleEntityProjectionQuery<E extends BaseEntity, R> extends SingleEntityProjectionQuery<E, R> implements AsyncQueryable<R> {

	public AsyncSingleEntityProjectionQuery(Class<R> returnType, ProjectionQueryBuilder<E, R> queryBuilder, QueryParameters<E> queryParameters, TransactionManager transactionManager) {
		super(returnType, queryBuilder, queryParameters, transactionManager);
	}
}
