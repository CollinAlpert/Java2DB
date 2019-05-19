package com.github.collinalpert.java2db.pagination;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.CachingModule;
import com.github.collinalpert.java2db.modules.LazyModule;
import com.github.collinalpert.java2db.queries.EntityQuery;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Extended class that adds caching functionality to the pagination implementation.
 * Note that caching will not be available for pages that are fetched asynchronously.
 *
 * @author Collin Alpert
 */
public class CacheablePaginationResult<T extends BaseEntity> extends PaginationResult<T> {

	/**
	 * The duration an entry in the cache is valid to use for.
	 */
	private final Duration cacheExpiration;

	/**
	 * The caching module for {@link List} results.
	 */
	private final LazyModule<CachingModule<List<T>>> listCache;

	/**
	 * The caching module for {@link Stream} results.
	 */
	private final LazyModule<CachingModule<Stream<T>>> streamCache;

	/**
	 * The caching module for array results.
	 */
	private final LazyModule<CachingModule<T[]>> arrayCache;

	/**
	 * Constructor that allows the creation of a cached pagination.
	 * To obtain an instance, please use the {@code createPagination} methods in the {@link com.github.collinalpert.java2db.services.BaseService}
	 *
	 * @param queries         The queries that represent the operations of retrieving specific pages from the database.
	 * @param cacheExpiration The duration a query result is valid for in the cache.
	 */
	public CacheablePaginationResult(List<EntityQuery<T>> queries, Duration cacheExpiration) {
		super(queries);
		this.cacheExpiration = cacheExpiration;
		this.listCache = new LazyModule<>(CachingModule::new);
		this.streamCache = new LazyModule<>(CachingModule::new);
		this.arrayCache = new LazyModule<>(CachingModule::new);
	}

	/**
	 * Gets a page by its identifier, or rather its number, an returns it as a {@link List}.
	 * If this particular page has already been requested and has not expired yet, it will be returned from the in-memory cache.
	 * Otherwise it will be fetched from the database.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link List} of entities which are displayed on the requested page.
	 */
	@Override
	public List<T> getPage(int number) {
		return listCache.getValue().getOrAdd(Integer.toString(number), () -> super.getPage(number), cacheExpiration);
	}

	/**
	 * Gets a page by its identifier, or rather its number, an returns it as a {@link Stream}.
	 * If this particular page has already been requested and has not expired yet, it will be returned from the in-memory cache.
	 * Otherwise it will be fetched from the database.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return A {@link Stream} of entities which are displayed on the requested page.
	 */
	@Override
	public Stream<T> getPageAsStream(int number) {
		return streamCache.getValue().getOrAdd(Integer.toString(number), () -> super.getPageAsStream(number), cacheExpiration);
	}

	/**
	 * Gets a page by its identifier, or rather its number, an returns it as an array.
	 * If this particular page has already been requested and has not expired yet, it will be returned from the in-memory cache.
	 * Otherwise it will be fetched from the database.
	 *
	 * @param number The number of the page. The first page has the index 1.
	 * @return An array of entities which are displayed on the requested page.
	 */
	@Override
	public T[] getPageAsArray(int number) {
		return arrayCache.getValue().getOrAdd(Integer.toString(number), () -> super.getPageAsArray(number), cacheExpiration);
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
		listCache.getValue().invalidate(name);
		streamCache.getValue().invalidate(name);
	}
}
