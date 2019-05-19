package com.github.collinalpert.java2db.queries;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Queryable<T> {

	// region Synchronous

	/**
	 * Gets the first value from the database result. This method should be used when only one result is expected.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned. If the value from the database is {@code null}, an empty {@code Optional} is also returned.
	 */
	Optional<T> getFirst();

	/**
	 * Executes the query and returns the result as a {@link List}
	 *
	 * @return A list of entities representing the result rows.
	 */
	List<T> toList();

	/**
	 * Executes the query and returns the result as a {@link Stream}
	 *
	 * @return A list of entities representing the result rows.
	 */
	Stream<T> toStream();

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	T[] toArray();

	// endregion

	// region Asynchronous

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #getFirst()
	 */
	default CompletableFuture<Optional<T>> getFirstAsync() {
		return CompletableFuture.supplyAsync(this::getFirst);

	}

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #getFirst()
	 */
	default CompletableFuture<Void> getFirstAsync(Consumer<? super Optional<T>> callback) {
		return this.getFirstAsync().thenAcceptAsync(callback);
	}

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

	// endregion

	/**
	 * Responsible for building and returning the individual DQL statement.
	 *
	 * @return The DQL statement which fetches data from the database.
	 */
	String getQuery();
}
