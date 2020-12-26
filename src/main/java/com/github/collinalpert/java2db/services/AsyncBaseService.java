package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.ConnectionConfiguration;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.async.*;
import com.github.collinalpert.java2db.queries.ordering.OrderTypes;
import com.github.collinalpert.java2db.utilities.FunctionUtils;
import com.github.collinalpert.lambda2sql.functions.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.github.collinalpert.java2db.utilities.Utilities.runnableHandling;
import static com.github.collinalpert.java2db.utilities.Utilities.supplierHandling;

/**
 * This class extends the functionality of a service class by adding support for asynchronous CRUD operations.
 * Inherit from this class instead of the {@link BaseService} if you want your services to be able to do stuff asynchronously.
 *
 * @author Collin Alpert
 * @see BaseService
 */
public class AsyncBaseService<E extends BaseEntity> extends BaseService<E> {

	protected AsyncBaseService(ConnectionConfiguration connectionConfiguration) {
		super(connectionConfiguration);
	}

	//region Create

	/**
	 * The asynchronous version of the {@link #create(BaseEntity)} method without custom exception handling and without a callback.
	 *
	 * @param instance The instance to create on the database asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(BaseEntity)
	 */
	public CompletableFuture<Void> createAsync(E instance) {
		return createAsync(instance, FunctionUtils.empty());
	}

	/**
	 * The asynchronous version of the {@link #create(BaseEntity)} method without custom exception handling.
	 *
	 * @param instance The instance to create on the database asynchronously.
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(BaseEntity)
	 */
	public CompletableFuture<Void> createAsync(E instance, Consumer<? super Integer> callback) {
		return createAsync(instance, callback, null);
	}

	/**
	 * The asynchronous version of the {@link #create(BaseEntity)} method.
	 *
	 * @param instance          The instance to create on the database asynchronously.
	 * @param callback          The action to apply to the result, once it is computed.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(BaseEntity)
	 */
	public CompletableFuture<Void> createAsync(E instance, Consumer<? super Integer> callback, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.supplyAsync(supplierHandling(() -> super.create(instance), exceptionHandling)).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #create(BaseEntity[])} method without custom exception handling.
	 *
	 * @param instances The instance to create on the database asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> createAsync(E... instances) {
		return createAsync(Arrays.asList(instances), null);
	}

	/**
	 * The asynchronous version of the {@link #create(BaseEntity[])} method.
	 *
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param instances         The instance to create on the database asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> createAsync(Consumer<SQLException> exceptionHandling, E... instances) {
		return createAsync(Arrays.asList(instances), exceptionHandling);
	}

	/**
	 * The asynchronous version of the {@link #create(List)} method without custom exception handling.
	 *
	 * @param instances The instance to create on the database asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(List)
	 */
	public CompletableFuture<Void> createAsync(List<E> instances) {
		return createAsync(instances, null);
	}

	/**
	 * The asynchronous version of the {@link #create(List)} method.
	 *
	 * @param instances         The instance to create on the database asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #create(List)
	 */
	public CompletableFuture<Void> createAsync(List<E> instances, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.create(instances), exceptionHandling));
	}

	//endregion

	//region Read

	//region Count

	/**
	 * The asynchronous version of the {@link #count()} method.
	 *
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #count()
	 */
	public CompletableFuture<Void> countAsync(Consumer<? super Integer> callback) {
		return CompletableFuture.supplyAsync(super::count).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #count(SqlPredicate)} method.
	 *
	 * @param predicate The condition to count by.
	 * @param callback  The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #count(SqlPredicate)
	 */
	public CompletableFuture<Void> countAsync(SqlPredicate<E> predicate, Consumer<? super Integer> callback) {
		return CompletableFuture.supplyAsync(() -> super.count(predicate)).thenAcceptAsync(callback);
	}

	//endregion

	//region Any

	/**
	 * The asynchronous version of the {@link #any()} method.
	 *
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #any()
	 */
	public CompletableFuture<Void> anyAsync(Consumer<? super Boolean> callback) {
		return CompletableFuture.supplyAsync(super::any).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #any(SqlPredicate)} method.
	 *
	 * @param predicate The condition to check for.
	 * @param callback  The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #any(SqlPredicate)
	 */
	public CompletableFuture<Void> anyAsync(SqlPredicate<E> predicate, Consumer<? super Boolean> callback) {
		return CompletableFuture.supplyAsync(() -> super.any(predicate)).thenAcceptAsync(callback);
	}

	//endregion

	//region Max

	/**
	 * The asynchronous version of the {@link #max(SqlFunction)} method.
	 *
	 * @param column   The column to get the maximum value of.
	 * @param callback The action to apply to the result, once it is computed.
	 * @param <T>      The generic type of the column. It is also the return type.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #max(SqlFunction)
	 */
	public <T> CompletableFuture<Void> maxAsync(SqlFunction<E, T> column, Consumer<? super T> callback) {
		return CompletableFuture.supplyAsync(() -> super.max(column)).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #max(SqlFunction, SqlPredicate)} method.
	 *
	 * @param column    The column to get the maximum value of.
	 * @param predicate The predicate to filter by.
	 * @param callback  The action to apply to the result, once it is computed.
	 * @param <T>       The generic type of the column. It is also the return type.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #max(SqlFunction, SqlPredicate)
	 */
	public <T> CompletableFuture<Void> maxAsync(SqlFunction<E, T> column, SqlPredicate<E> predicate, Consumer<? super T> callback) {
		return CompletableFuture.supplyAsync(() -> super.max(column, predicate)).thenAcceptAsync(callback);
	}

	//endregion

	//region Min

	/**
	 * The asynchronous version of the {@link #min(SqlFunction)} method.
	 *
	 * @param column   The column to get the minimum value of.
	 * @param callback The action to apply to the result, once it is computed.
	 * @param <T>      The generic type of the column. It is also the return type.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #min(SqlFunction)
	 */
	public <T> CompletableFuture<Void> minAsync(SqlFunction<E, T> column, Consumer<? super T> callback) {
		return CompletableFuture.supplyAsync(() -> super.min(column)).thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #min(SqlFunction, SqlPredicate)} method.
	 *
	 * @param column    The column to get the minimum value of.
	 * @param predicate The predicate to filter by.
	 * @param callback  The action to apply to the result, once it is computed.
	 * @param <T>       The generic type of the column. It is also the return type.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #min(SqlFunction, SqlPredicate)
	 */
	public <T> CompletableFuture<Void> minAsync(SqlFunction<E, T> column, SqlPredicate<E> predicate, Consumer<? super T> callback) {
		return CompletableFuture.supplyAsync(() -> super.min(column, predicate)).thenAcceptAsync(callback);
	}

	//endregion

	//region HasDuplicates

	/**
	 * The asynchronous version of the {@link #hasDuplicates(SqlFunction)} method.
	 *
	 * @param column   The column to check for duplicates in.
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #hasDuplicates(SqlFunction)
	 */
	public CompletableFuture<Void> hasDuplicatesAsync(SqlFunction<E, ?> column, Consumer<? super Boolean> callback) {
		return CompletableFuture.supplyAsync(() -> super.hasDuplicates(column)).thenAcceptAsync(callback);
	}

	//endregion

	//region Query

	protected AsyncEntityQuery<E> createAsyncQuery() {
		return new AsyncEntityQuery<>(super.type, super.connectionConfiguration);
	}

	protected AsyncSingleEntityQuery<E> createAsyncSingleQuery() {
		return new AsyncSingleEntityQuery<>(super.type, super.connectionConfiguration);
	}

	/**
	 * The asynchronous version of the {@link #getFirst(SqlPredicate)} method.
	 *
	 * @param predicate The condition to get the result by.
	 * @param callback  The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getFirst(SqlPredicate)
	 */
	public CompletableFuture<Void> getFirstAsync(SqlPredicate<E> predicate, Consumer<? super Optional<E>> callback) {
		return CompletableFuture.supplyAsync(() -> super.getFirst(predicate)).thenAcceptAsync(callback);
	}

	@Override
	public AsyncSingleEntityQuery<E> getSingle(SqlPredicate<E> predicate) {
		return createAsyncSingleQuery().where(predicate);
	}

	/**
	 * Retrieves list of entities which match the predicate.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL statement.
	 * @return A list of entities matching the result of the query.
	 */
	@Override
	public AsyncEntityQuery<E> getMultiple(SqlPredicate<E> predicate) {
		return createAsyncQuery().where(predicate);
	}

	/**
	 * The asynchronous version of the {@link #getById(int)} method.
	 *
	 * @param id       An id to find a specific entity by.
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getById(int)
	 */
	public CompletableFuture<Void> getByIdAsync(int id, Consumer<? super Optional<E>> callback) {
		return getSingle(x -> x.getId() == id).firstAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #getAll()} method.
	 *
	 * @param callback The action to apply to the result, once it is computed.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getAll()
	 */
	public CompletableFuture<Void> getAllAsync(Consumer<? super List<E>> callback) {
		return createAsyncQuery().toListAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #getAll(SqlFunction)} method.
	 *
	 * @param callback The action to apply to the result, once it is computed.
	 * @param orderBy  The property to order by.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getAll(SqlFunction)
	 */
	public CompletableFuture<Void> getAllAsync(Consumer<? super List<E>> callback, SqlFunction<E, ?> orderBy) {
		return createAsyncQuery().orderBy(orderBy).toListAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #getAll(SqlFunction[])} method.
	 *
	 * @param callback The action to apply to the result, once it is computed.
	 * @param orderBy  The properties to order by.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getAll(SqlFunction[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> getAllAsync(Consumer<? super List<E>> callback, SqlFunction<E, ?>... orderBy) {
		return getAllAsync(callback, OrderTypes.ASCENDING, orderBy);
	}

	/**
	 * The asynchronous version of the {@link #getAll(SqlFunction, OrderTypes)} method.
	 *
	 * @param callback    The action to apply to the result, once it is computed.
	 * @param orderBy     The property to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getAll(SqlFunction, OrderTypes)
	 */
	public CompletableFuture<Void> getAllAsync(Consumer<? super List<E>> callback, OrderTypes sortingType, SqlFunction<E, ?> orderBy) {
		return createAsyncQuery().orderBy(orderBy, sortingType).toListAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #getAll(SqlFunction[], OrderTypes)} method.
	 *
	 * @param callback    The action to apply to the result, once it is computed.
	 * @param orderBy     The properties to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #getAll(SqlFunction[], OrderTypes)
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> getAllAsync(Consumer<? super List<E>> callback, OrderTypes sortingType, SqlFunction<E, ?>... orderBy) {
		return createAsyncQuery().orderBy(orderBy, sortingType).toListAsync(callback);
	}

	//endregion

	//endregion

	//region Update

	/**
	 * The asynchronous version of the {@link #update(BaseEntity)} method without custom exception handling.
	 *
	 * @param instance The instance to update asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(BaseEntity)
	 */
	public CompletableFuture<Void> updateAsync(E instance) {
		return updateAsync(instance, null);
	}

	/**
	 * The asynchronous version of the {@link #update(BaseEntity)} method.
	 *
	 * @param instance          The instance to update asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(BaseEntity)
	 */
	public CompletableFuture<Void> updateAsync(E instance, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.update(instance), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #update(BaseEntity[])} without custom exception handling.
	 *
	 * @param instances The instances to update asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> updateAsync(E... instances) {
		return updateAsync(Arrays.asList(instances), null);
	}

	/**
	 * The asynchronous version of the {@link #update(BaseEntity[])}.
	 *
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param instances         The instances to update asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> updateAsync(Consumer<SQLException> exceptionHandling, E... instances) {
		return updateAsync(Arrays.asList(instances), exceptionHandling);
	}

	/**
	 * The asynchronous version of the {@link #update(List)} without custom exception handling.
	 *
	 * @param instances The instances to update asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(List)
	 */
	public CompletableFuture<Void> updateAsync(List<E> instances) {
		return updateAsync(instances, null);
	}

	/**
	 * The asynchronous version of the {@link #update(List)}.
	 *
	 * @param instances         The instances to update asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(List)
	 */
	public CompletableFuture<Void> updateAsync(List<E> instances, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.update(instances), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #update(int, SqlFunction, SqlFunction)} method without custom exception handling.
	 *
	 * @param entityId         The id of the row to update.
	 * @param column           The column to update.
	 * @param newValueFunction The function to calculate the new value.
	 * @param <R>              The type of the column to update.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 */
	public <R> CompletableFuture<Void> updateAsync(int entityId, SqlFunction<E, R> column, SqlFunction<E, R> newValueFunction) {
		return this.updateAsync(entityId, column, newValueFunction, null);
	}

	/**
	 * The asynchronous version of the {@link #update(int, SqlFunction, SqlFunction)} method.
	 *
	 * @param entityId          The id of the row to update.
	 * @param column            The column to update.
	 * @param newValueFunction  The function to calculate the new value.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param <R>               The type of the column to update.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 */
	public <R> CompletableFuture<Void> updateAsync(int entityId, SqlFunction<E, R> column, SqlFunction<E, R> newValueFunction, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.update(entityId, column, newValueFunction), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #update(int, SqlFunction, Object)} method without custom exception handling.
	 *
	 * @param entityId The id of the instance to update asynchronously.
	 * @param column   The column of the entity to update.
	 * @param newValue The new value of the column.
	 * @param <R>      The data type of the column to update. It must be the same as the data type of the new value.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(int, SqlFunction, Object)
	 */
	public <R> CompletableFuture<Void> updateAsync(int entityId, SqlFunction<E, R> column, R newValue) {
		return updateAsync(x -> x.getId() == entityId, column, newValue, null);
	}

	/**
	 * The asynchronous version of the {@link #update(int, SqlFunction, Object)} method.
	 *
	 * @param entityId          The id of the instance to update asynchronously.
	 * @param column            The column of the entity to update.
	 * @param newValue          The new value of the column.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param <R>               The data type of the column to update. It must be the same as the data type of the new value.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(int, SqlFunction, Object)
	 */
	public <R> CompletableFuture<Void> updateAsync(int entityId, SqlFunction<E, R> column, R newValue, Consumer<SQLException> exceptionHandling) {
		return updateAsync(x -> x.getId() == entityId, column, newValue, exceptionHandling);
	}

	/**
	 * The asynchronous version of the {@link #update(SqlPredicate, SqlFunction, Object)} method without custom exception handling.
	 *
	 * @param condition The condition to find records by which will be updated asynchronously.
	 * @param column    The column of the entity to update.
	 * @param newValue  The new value of the column.
	 * @param <R>       The data type of the column to update. It must be the same as the data type of the new value.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(SqlPredicate, SqlFunction, Object)
	 */
	public <R> CompletableFuture<Void> updateAsync(SqlPredicate<E> condition, SqlFunction<E, R> column, R newValue) {
		return updateAsync(condition, column, newValue, null);
	}

	/**
	 * The asynchronous version of the {@link #update(SqlPredicate, SqlFunction, Object)} method.
	 *
	 * @param condition         The condition to find records by which will be updated asynchronously.
	 * @param column            The column of the entity to update.
	 * @param newValue          The new value of the column.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param <R>               The data type of the column to update. It must be the same as the data type of the new value.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #update(SqlPredicate, SqlFunction, Object)
	 */
	public <R> CompletableFuture<Void> updateAsync(SqlPredicate<E> condition, SqlFunction<E, R> column, R newValue, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.update(condition, column, newValue), exceptionHandling));
	}

	//endregion

	//region Delete

	/**
	 * The asynchronous version of the {@link #delete(BaseEntity)}  method without custom exception handling.
	 *
	 * @param instance The instance to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(BaseEntity)
	 */
	public CompletableFuture<Void> deleteAsync(E instance) {
		return deleteAsync(instance, null);
	}

	/**
	 * The asynchronous version of the {@link #delete(BaseEntity)} method.
	 *
	 * @param instance          The instance to delete asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(BaseEntity)
	 */
	public CompletableFuture<Void> deleteAsync(E instance, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(instance), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #delete(int)} method without custom exception handling.
	 *
	 * @param id The id of the instance to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(int)
	 */
	public CompletableFuture<Void> deleteAsync(int id) {
		return deleteAsync(id, null);
	}

	/**
	 * The asynchronous version of the {@link #delete(int)} method.
	 *
	 * @param id                The id of the instance to delete asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(int)
	 */
	public CompletableFuture<Void> deleteAsync(int id, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(id), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #delete(List)} method without custom exception handling.
	 *
	 * @param entities The instances to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(List)
	 */
	public CompletableFuture<Void> deleteAsync(List<E> entities) {
		return deleteAsync(entities, null);
	}

	/**
	 * The asynchronous version of the {@link #delete(List)} method.
	 *
	 * @param entities          The instances to delete asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(List)
	 */
	public CompletableFuture<Void> deleteAsync(List<E> entities, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(entities), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #delete(BaseEntity[])} method without custom exception handling.
	 *
	 * @param entities The instances to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> deleteAsync(E... entities) {
		return deleteAsync(null, entities);
	}

	/**
	 * The asynchronous version of the {@link #delete(BaseEntity[])} method.
	 *
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param entities          The instances to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(BaseEntity[])
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> deleteAsync(Consumer<SQLException> exceptionHandling, E... entities) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(Arrays.asList(entities)), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #delete(int...)} method without custom exception handling.
	 *
	 * @param ids The ids of the instances to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(int...)
	 */
	public CompletableFuture<Void> deleteAsync(int... ids) {
		return deleteAsync(null, ids);
	}

	/**
	 * The asynchronous version of the {@link #delete(int...)} method.
	 *
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @param ids               The ids of the instances to delete asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(int...)
	 */
	public CompletableFuture<Void> deleteAsync(Consumer<SQLException> exceptionHandling, int... ids) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(ids), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #delete(SqlPredicate)} method without custom exception handling.
	 *
	 * @param predicate A condition to delete records on the database by asynchronously.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(SqlPredicate)
	 */
	public CompletableFuture<Void> deleteAsync(SqlPredicate<E> predicate) {
		return deleteAsync(predicate, null);
	}

	/**
	 * The asynchronous version of the {@link #delete(SqlPredicate)} method.
	 *
	 * @param predicate         A condition to delete records on the database by asynchronously.
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #delete(SqlPredicate)
	 */
	public CompletableFuture<Void> deleteAsync(SqlPredicate<E> predicate, Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(() -> super.delete(predicate), exceptionHandling));
	}

	/**
	 * The asynchronous version of the {@link #truncateTable()} method without custom exception handling.
	 *
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #truncateTable()
	 */
	public CompletableFuture<Void> truncateTableAsync() {
		return truncateTableAsync(null);
	}

	/**
	 * The asynchronous version of the {@link #truncateTable()} method.
	 *
	 * @param exceptionHandling Custom exception handling for the checked exception thrown by the underlying method.
	 * @return A {@link CompletableFuture} which represents the asynchronous operation.
	 * @see #truncateTable()
	 */
	public CompletableFuture<Void> truncateTableAsync(Consumer<SQLException> exceptionHandling) {
		return CompletableFuture.runAsync(runnableHandling(super::truncateTable, exceptionHandling));
	}

	//endregion
}