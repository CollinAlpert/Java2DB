package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.IMapper;
import com.github.collinalpert.java2db.services.AsyncBaseService;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a query that can be executed asynchronously.
 *
 * @author Collin Alpert
 * @see Query
 */
public class AsyncQuery<T extends BaseEntity> extends Query<T> {

	/**
	 * Constructor for creating a DQL statement for a given entity that will be executed asynchronously.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link AsyncBaseService}.
	 *
	 * @param type   The entity to query.
	 * @param mapper The mapper for mapping entities.
	 */
	public AsyncQuery(Class<T> type, IMapper<T> mapper) {
		super(type, mapper);
	}

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #getFirst()
	 */
	public CompletableFuture<Optional<T>> getFirstAsync() {
		return CompletableFuture.supplyAsync(super::getFirst);

	}

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action.
	 * @see #getFirst()
	 */
	public CompletableFuture<Void> getFirstAsync(Consumer<? super Optional<T>> callback) {
		return this.getFirstAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toList()
	 */
	public CompletableFuture<List<T>> toListAsync() {
		return CompletableFuture.supplyAsync(super::toList);
	}

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action.
	 * @see #toList()
	 */
	public CompletableFuture<Void> toListAsync(Consumer<? super List<T>> callback) {
		return this.toListAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toStream()
	 */
	public CompletableFuture<Stream<T>> toStreamAsync() {
		return CompletableFuture.supplyAsync(super::toStream);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action.
	 * @see #toStream()
	 */
	public CompletableFuture<Void> toStreamAsync(Consumer<? super Stream<T>> callback) {
		return toStreamAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toArray()
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<T[]> toArrayAsync() {
		return CompletableFuture.supplyAsync(super::toArray);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action.
	 * @see #toArray()
	 */
	public CompletableFuture<Void> toArrayAsync(Consumer<? super T[]> callback) {
		return toArrayAsync().thenAcceptAsync(callback);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public AsyncQuery<T> where(SqlPredicate<T> predicate) {
		super.where(predicate);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public AsyncQuery<T> orWhere(SqlPredicate<T> predicate) {
		super.orWhere(predicate);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public AsyncQuery<T> limit(int limit, int offset) {
		super.limit(limit, offset);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public AsyncQuery<T> limit(int limit) {
		super.limit(limit);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@SafeVarargs
	@Override
	public final AsyncQuery<T> orderBy(SqlFunction<T, ?>... functions) {
		super.orderBy(functions);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@SafeVarargs
	@Override
	public final AsyncQuery<T> orderBy(OrderTypes type, SqlFunction<T, ?>... functions) {
		super.orderBy(type, functions);
		return this;
	}
}
