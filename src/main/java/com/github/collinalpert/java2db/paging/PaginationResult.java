package com.github.collinalpert.java2db.paging;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.Query;

import java.util.List;
import java.util.stream.Stream;

/**
 * Class for a simple pagination implementation.
 *
 * @author Collin Alpert
 */
public class PaginationResult<T extends BaseEntity> {

	protected final List<Query<T>> queries;

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
		return queries.get(number - 1).toList();
	}

	/**
	 * Retrieves a specific page represented by a {@link Stream}. Only then will a query to the database be executed.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link Stream} of entities on this page.
	 */
	public Stream<T> getPageAsStream(int number) {
		pageNumberCheck(number);
		return queries.get(number - 1).toStream();
	}
}
