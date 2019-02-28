package com.github.collinalpert.java2db.pagination;

import com.github.collinalpert.java2db.caching.CachingModule;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.Query;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Extended class that adds caching functionality to the pagination implementation.
 *
 * @author Collin Alpert
 */
public class CacheablePaginationResult<T extends BaseEntity> extends PaginationResult<T> {

	/**
	 * The duration an entry in the cache is valid to use for.
	 */
	private final Duration cacheExpiration;

	/**
	 * The caching module for {@link Stream} results.
	 */
	private final CachingModule<List<T>> listCache;

	/**
	 * The caching module for {@link Stream} results.
	 */
	private final CachingModule<Stream<T>> streamCache;

	/**
	 * Constructor that allows the creation of a cached pagination.
	 * To obtain an instance, please use the {@code createPagination} methods in the {@link com.github.collinalpert.java2db.services.BaseService}
	 *
	 * @param queries         The queries that represent the operations of retrieving specific pages from the database.
	 * @param cacheExpiration The duration a query result is valid for in the cache.
	 */
	public CacheablePaginationResult(List<Query<T>> queries, Duration cacheExpiration) {
		super(queries);
		this.cacheExpiration = cacheExpiration;
		this.listCache = new CachingModule<>();
		this.streamCache = new CachingModule<>();
	}

	/**
	 * Gets a page by its identifier, or rather its number, an returns it as a {@link List}.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link List} of entities which are displayed on the requested page.
	 */
	@Override
	public List<T> getPage(int number) {
		return listCache.getOrAdd(Integer.toString(number), () -> super.getPage(number), cacheExpiration);
	}

	/**
	 * Gets a page by its identifier, or rather its number, an returns it as a {@link Stream}.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link Stream} of entities which are displayed on the requested page.
	 */
	@Override
	public Stream<T> getPageAsStream(int number) {
		return streamCache.getOrAdd(Integer.toString(number), () -> super.getPageAsStream(number), cacheExpiration);
	}

	/**
	 * Marks the entire cache of the pagination as invalid, causing a reload the next time and value is requested.
	 * This call is equivalent to {@code invalidateCache(null)}.
	 */
	public void invalidateCaches() {
		invalidateCache(null);
	}

	/**
	 * Marks a specific value in the cache as invalid.
	 * This will cause a reload of the value the next time it is requested from the cache.
	 *
	 * @param name The name, or rather the page number, of the value in the cache.
	 */
	public void invalidateCache(String name) {
		listCache.invalidate(name);
		streamCache.invalidate(name);
	}
}
