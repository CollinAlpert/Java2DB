package com.github.collinalpert.java2db.modules;

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * A helper module which contains functionality for basic caching.
 * Its main task to cache query results.
 *
 * @author Collin Alpert
 */
public class CachingModule<T> {

	private final Map<String, Entry> cacheEntries;

	public CachingModule() {
		cacheEntries = new HashMap<>();
	}

	/**
	 * Gets an entry from the cache, or creates it if it does not exist using the passed {@code valueFactory}.
	 *
	 * @param name         The name of the cache entry.
	 * @param valueFactory The {@link Supplier} of data, in case the cache does not have an entry or the entry is expired.
	 * @param expiration   The duration the cache is valid. After this duration is exceeded,
	 *                     the value will be cached from the passed {@code valueFactory}
	 * @return The requested value from the cache, if it exists. Otherwise the value from the {@code valueFactory} will be returned.
	 */
	public T getOrAdd(String name, Supplier<T> valueFactory, Duration expiration) {
		final Entry entry;
		if (cacheEntries.containsKey(name) && (entry = cacheEntries.get(name)) != null && !entry.isExpired()) {
			return entry.getValue();
		}

		var value = valueFactory.get();
		cacheEntries.put(name, new Entry(value, LocalDateTime.now().plus(expiration)));
		return value;
	}

	/**
	 * Invalidates, or "clears", the contents of this cache.
	 */
	public void invalidate() {
		invalidate(null);
	}

	/**
	 * Invalidates, or rather removes, a specific cache entry.
	 * This will prompt a reload from the database the next time a value with this cache name is requested.
	 *
	 * @param name The name of the entry in the cache.
	 */
	public void invalidate(String name) {
		if (name == null) {
			cacheEntries.clear();
			return;
		}

		cacheEntries.remove(name);
	}

	private class Entry {

		private final T value;
		private final LocalDateTime expirationDate;

		private Entry(T value, LocalDateTime expirationDate) {
			this.value = value;
			this.expirationDate = expirationDate;
		}

		public T getValue() {
			return this.value;
		}

		private boolean isExpired() {
			return LocalDateTime.now().isAfter(this.expirationDate);
		}
	}
}
