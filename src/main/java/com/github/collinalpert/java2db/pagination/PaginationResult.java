package com.github.collinalpert.java2db.pagination;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.EntityQuery;
import com.github.collinalpert.java2db.queries.ordering.OrderTypes;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Class for a simple pagination implementation.
 *
 * @author Collin Alpert
 */
public class PaginationResult<T extends BaseEntity> {

	protected final List<EntityQuery<T>> queries;
	private SqlFunction<T, ?>[] orderFunctions;
	private OrderTypes orderType;

	public PaginationResult(List<EntityQuery<T>> queries) {
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
			throw new IllegalArgumentException("The first page starts at the number 1.");
		}
	}

	/**
	 * Retrieves a specific page represented by a {@link List}. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the number 1.
	 * @return A {@link List} of entities on this page.
	 */
	public List<T> getPage(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).orderBy(this.orderFunctions, this.orderType).toList();
	}

	/**
	 * The asynchronous version of the {@link #getPage(int)} method.
	 *
	 * @param number   The number of the page. The first page has the number 1.
	 * @param callback The callback to be executed once the page has been fetched.
	 * @return A {@link CompletableFuture} representing the asynchronous operation.
	 * @see #getPage(int)
	 */
	public CompletableFuture<Void> getPageAsync(int number, Consumer<? super List<T>> callback) {
		return CompletableFuture.supplyAsync(() -> getPage(number)).thenAcceptAsync(callback);
	}

	/**
	 * Retrieves a specific page represented by a {@link Stream}. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the number 1.
	 * @return A {@link Stream} of entities on this page.
	 */
	public Stream<T> getPageAsStream(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).orderBy(this.orderFunctions, this.orderType).toStream();
	}

	/**
	 * The asynchronous version of the {@link #getPageAsStream(int)} method.
	 *
	 * @param number   The number of the page. The first page has the number 1.
	 * @param callback The callback to be executed once the page has been fetched.
	 * @return A {@link CompletableFuture} representing the asynchronous operation.
	 * @see #getPageAsStream(int)
	 */
	public CompletableFuture<Void> getPageAsStreamAsync(int number, Consumer<? super Stream<T>> callback) {
		return CompletableFuture.supplyAsync(() -> getPageAsStream(number)).thenAcceptAsync(callback);
	}

	/**
	 * Retrieves a specific page represented by an array. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the number 1.
	 * @return An array of entities on this page.
	 */
	public T[] getPageAsArray(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).orderBy(this.orderFunctions, this.orderType).toArray();
	}

	/**
	 * The asynchronous version of the {@link #getPageAsArray(int)} method.
	 *
	 * @param number   The number of the page. The first page has the number 1.
	 * @param callback The callback to be executed once the page has been fetched.
	 * @return A {@link CompletableFuture} representing the asynchronous operation.
	 * @see #getPageAsArray(int)
	 */
	public CompletableFuture<Void> getPageAsArrayAsync(int number, Consumer<? super T[]> callback) {
		return CompletableFuture.supplyAsync(() -> getPageAsArray(number)).thenAcceptAsync(callback);
	}

	/**
	 * Adds ascending ORDER BY statements to the queries executed for the pages in a coalescing manner.
	 * Note that this will order the entire pagination structure and not every page separately.
	 *
	 * @param orderFunctions The columns to order by.
	 * @return The object with an ORDER BY statement
	 */
	@SafeVarargs
	public final PaginationResult<T> orderBy(SqlFunction<T, ?>... orderFunctions) {
		return orderBy(OrderTypes.ASCENDING, orderFunctions);
	}

	/**
	 * Adds ORDER BY statements to the queries executed for the pages in a coalescing manner.
	 * Note that this will order the entire pagination structure and not every page separately.
	 *
	 * @param orderType      The direction of order.
	 * @param orderFunctions The columns to order by.
	 * @return The object with an ORDER BY statement
	 */
	@SafeVarargs
	public final PaginationResult<T> orderBy(OrderTypes orderType, SqlFunction<T, ?>... orderFunctions) {
		this.orderFunctions = orderFunctions;
		this.orderType = orderType;

		return this;
	}
}
