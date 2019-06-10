package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.queries.SingleQueryable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Collin Alpert
 */
public interface AsyncSingleQueryable<T> extends SingleQueryable<T> {

	/**
	 * The asynchronous version of the {@link #first()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #first()
	 */
	default CompletableFuture<Optional<T>> firstAsync() {
		return CompletableFuture.supplyAsync(this::first);

	}

	/**
	 * The asynchronous version of the {@link #first()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #first()
	 */
	default CompletableFuture<Void> firstAsync(Consumer<? super Optional<T>> callback) {
		return this.firstAsync().thenAcceptAsync(callback);
	}
}
