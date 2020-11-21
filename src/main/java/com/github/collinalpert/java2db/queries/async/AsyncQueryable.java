package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.queries.Queryable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
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

	/**
	 * The asynchronous version of the {@link #toMap(Function)} method.
	 *
	 * @param keyMapping The field representing the keys of the map.
	 * @param <K>        The type of the keys in the map.
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toMap(Function)
	 */
	default <K> CompletableFuture<Map<K, T>> toMapAsync(Function<T, K> keyMapping) {
		return CompletableFuture.supplyAsync(() -> this.toMap(keyMapping));
	}

	/**
	 * The asynchronous version of the {@link #toMap(Function)} method.
	 *
	 * @param keyMapping The field representing the keys of the map.
	 * @param callback   The action to be applied to the result once it is fetched from the database.
	 * @param <K>        The type of the keys in the map.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toMap(Function)
	 */
	default <K> CompletableFuture<Void> toMapAsync(Function<T, K> keyMapping, Consumer<? super Map<K, T>> callback) {
		return toMapAsync(keyMapping).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toMap(Function, Function)} method.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @param <K>          The type of the keys in the map.
	 * @param <V>          The type of the values in the map.
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toMap(Function, Function)
	 */
	default <K, V> CompletableFuture<Map<K, V>> toMapAsync(Function<T, K> keyMapping, Function<T, V> valueMapping) {
		return CompletableFuture.supplyAsync(() -> this.toMap(keyMapping, valueMapping));
	}

	/**
	 * The asynchronous version of the {@link #toMap(Function, Function)} method.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @param callback     The action to be applied to the result once it is fetched from the database.
	 * @param <K>          The type of the keys in the map.
	 * @param <V>          The type of the values in the map.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 */
	default <K, V> CompletableFuture<Void> toMapAsync(Function<T, K> keyMapping, Function<T, V> valueMapping, Consumer<? super Map<K, V>> callback) {
		return toMapAsync(keyMapping, valueMapping).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toSet()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toSet()
	 */
	default CompletableFuture<Set<T>> toSetAsync() {
		return CompletableFuture.supplyAsync(this::toSet);
	}

	/**
	 * The asynchronous version of the {@link #toSet()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toSet()
	 */
	default CompletableFuture<Void> toSetAsync(Consumer<? super Set<T>> callback) {
		return toSetAsync().thenAcceptAsync(callback);
	}
}
