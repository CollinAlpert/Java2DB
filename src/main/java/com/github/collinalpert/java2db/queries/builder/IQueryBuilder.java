package com.github.collinalpert.java2db.queries.builder;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.QueryParameters;

/**
 * A general interface which describes building an SQL query.
 *
 * @author Collin Alpert
 */
public interface IQueryBuilder<E extends BaseEntity> {

	String build(QueryParameters<E> queryParameters);
}
