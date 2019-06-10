package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.queries.Queryable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface AsyncQueryable<T> extends Queryable<T>, AsyncSingleQueryable<T> {

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toList()
	 */
	default CompletableFuture<List<T>> toListAsync() {
		return CompletableFuture.supplyAsync(this::toList);
	}

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toList()
	 */
	default CompletableFuture<Void> toListAsync(Consumer<? super List<T>> callback) {
		return this.toListAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toStream()
	 */
	default CompletableFuture<Stream<T>> toStreamAsync() {
		return CompletableFuture.supplyAsync(this::toStream);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toStream()
	 */
	default CompletableFuture<Void> toStreamAsync(Consumer<? super Stream<T>> callback) {
		return toStreamAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toArray()
	 */
	default CompletableFuture<T[]> toArrayAsync() {
		return CompletableFuture.supplyAsync(this::toArray);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toArray()
	 */
	default CompletableFuture<Void> toArrayAsync(Consumer<? super T[]> callback) {
		return toArrayAsync().thenAcceptAsync(callback);
	}
}
