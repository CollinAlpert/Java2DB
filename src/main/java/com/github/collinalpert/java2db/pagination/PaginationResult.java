package com.github.collinalpert.java2db.pagination;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.OrderTypes;
import com.github.collinalpert.java2db.queries.Query;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

import java.util.List;
import java.util.stream.Stream;

/**
 * Class for a simple pagination implementation.
 *
 * @author Collin Alpert
 */
public class PaginationResult<T extends BaseEntity> {

	protected final List<Query<T>> queries;
	private SqlFunction<T, ?> orderFunction;
	private OrderTypes orderType;

	public PaginationResult(List<Query<T>> queries) {
		this.queries = queries;
	}

	/**
	 * Gets the amount of pages created to split up a query.
	 *
	 * @return The number of pages created for the result.
	 */
	public int getNumberOfPages() {
		return queries.size();
	}

	/**
	 * Checks a requested page number for validity.
	 *
	 * @param pageNumber The page number to validate.
	 */
	private void pageNumberCheck(int pageNumber) {
		if (pageNumber > queries.size()) {
			throw new IllegalArgumentException("The requested page number exceeds the total number of pages.");
		}

		if (pageNumber == 0) {
			throw new IllegalArgumentException("The first page starts at the index 1.");
		}
	}

	/**
	 * Retrieves a specific page represented by a {@link List}. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link List} of entities on this page.
	 */
	public List<T> getPage(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).orderBy(this.orderFunction, this.orderType).toList();
	}

	/**
	 * Retrieves a specific page represented by a {@link Stream}. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link Stream} of entities on this page.
	 */
	public Stream<T> getPageAsStream(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).orderBy(this.orderFunction, this.orderType).toStream();
	}

	/**
	 * An overload of the {@link #orderBy(SqlFunction, OrderTypes)} method. Will order in an ascending fashion.
	 *
	 * @param orderFunction The function to order by.
	 * @return This object with the added ordering feature.
	 * @see #orderBy(SqlFunction, OrderTypes)
	 */
	public PaginationResult<T> orderBy(SqlFunction<T, ?> orderFunction) {
		return orderBy(orderFunction, OrderTypes.ASCENDING);
	}

	/**
	 * Adds an ORDER BY statement to the queries executed for the pages.
	 * Note that this will order the entire pagination structure and not every page separately.
	 *
	 * @param orderFunction The function to order by.
	 * @param orderType     The direction of order.
	 * @return This object with the added ordering feature.
	 */
	public PaginationResult<T> orderBy(SqlFunction<T, ?> orderFunction, OrderTypes orderType) {
		this.orderFunction = orderFunction;
		this.orderType = orderType;
		return this;
	}
}
