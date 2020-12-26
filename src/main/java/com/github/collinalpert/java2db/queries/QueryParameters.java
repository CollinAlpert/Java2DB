package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.ordering.*;
import com.github.collinalpert.lambda2sql.functions.*;

import java.util.*;

/**
 * @author Collin Alpert
 */
public class QueryParameters<E extends BaseEntity> {

	private SqlPredicate<E> whereClause;
	private OrderTypes orderType;
	private OrderByClause<E> orderByClause;
	private List<SqlFunction<E, ?>> groupByClause;
	private Integer limit;
	private int limitOffset;
	private boolean distinct;

	public SqlPredicate<E> getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(SqlPredicate<E> whereClause) {
		this.whereClause = whereClause;
	}

	public void appendLogicalAndWhereClause(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.and(predicate);
	}

	public void appendLogicalOrWhereClause(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.or(predicate);
	}

	public OrderTypes getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderTypes orderType) {
		this.orderType = orderType;
	}

	public OrderByClause<E> getOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(SqlFunction<E, ?> orderByColumn, OrderTypes orderType) {
		this.orderByClause = new OrderByClause<>(orderByColumn, orderType);
	}

	public void setOrderByClause(List<SqlFunction<E, ?>> orderByColumns, OrderTypes orderType) {
		this.orderByClause = new OrderByClause<>(orderByColumns, orderType);
	}

	public void addOrderByColumns(SqlFunction<E, ?> orderByColumn, OrderTypes orderType) {
		try {
			this.orderByClause.addStatement(orderByColumn, orderType);
		} catch (NullPointerException e) {
			throw new IllegalStateException("Please use the '.orderBy' method before using the '.thenBy' method.", e);
		}
	}

	public void addOrderByColumns(List<SqlFunction<E, ?>> orderByColumns, OrderTypes orderType) {
		try {
			this.orderByClause.addStatement(orderByColumns, orderType);
		} catch (NullPointerException e) {
			throw new IllegalStateException("Please use the '.orderBy' method before using the '.thenBy' method.", e);
		}
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public int getLimitOffset() {
		return limitOffset;
	}

	public void setLimitOffset(int limitOffset) {
		this.limitOffset = limitOffset;
	}

	public List<SqlFunction<E, ?>> getGroupByClause() {
		return this.groupByClause;
	}

	public void setGroupBy(SqlFunction<E, ?> groupBy) {
		this.groupByClause = Collections.singletonList(groupBy);
	}

	public void setGroupBy(List<SqlFunction<E, ?>> groupBy) {
		this.groupByClause = groupBy;
	}

	public boolean getDistinct() {
		return distinct;
	}

	public void setDistinct() {
		this.distinct = true;
	}
}
