package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.ArrayModule;
import com.github.collinalpert.java2db.queries.builder.IQueryBuilder;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A query which represents a projection from an {@link EntityQuery} to a single column on the database.
 *
 * @param <E> The entity which the query is supposed to be executed for.
 * @param <R> The return type of the projection this query represents.
 * @author Collin Alpert
 */
public class EntityProjectionQuery<E extends BaseEntity, R> implements Queryable<R> {

	private final Class<R> returnType;
	private final IQueryBuilder<E> queryBuilder;
	private final QueryParameters<E> queryParameters;
	private final ConnectionConfiguration connectionConfiguration;

	public EntityProjectionQuery(Class<R> returnType, IQueryBuilder<E> queryBuilder, QueryParameters<E> queryParameters, ConnectionConfiguration connectionConfiguration) {
		this.returnType = returnType;
		this.queryBuilder = queryBuilder;
		this.queryParameters = queryParameters;
		this.connectionConfiguration = connectionConfiguration;
	}

	@Override
	public Optional<R> first() {
		try (var connection = new DBConnection(this.connectionConfiguration);
			 var result = connection.execute(getQuery())) {
			if (result.next()) {
				return Optional.ofNullable(result.getObject(1, this.returnType));
			}

			return Optional.empty();
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	@Override
	public List<R> toList() {
		var list = new ArrayList<R>();
		return resultHandling(list, List::add, Collections.emptyList(), Function.identity());
	}

	@Override
	public Stream<R> toStream() {
		var streamBuilder = Stream.<R>builder();
		return resultHandling(streamBuilder, Stream.Builder::accept, Stream.empty(), Stream.Builder::build);
	}

	@Override
	public R[] toArray() {
		var arrayModule = new ArrayModule<>(this.returnType, 20);
		@SuppressWarnings("unchecked")
		var defaultValue = (R[]) Array.newInstance(this.returnType, 0);
		return resultHandling(arrayModule, ArrayModule::addElement, defaultValue, ArrayModule::getArray);
	}

	/**
	 * Executes a new query and returns the result as a {@link Map}.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @return A map containing the result of the query.
	 */
	@Override
	public <K, V> Map<K, V> toMap(Function<R, K> keyMapping, Function<R, V> valueMapping) {
		return resultHandling(new HashMap<>(), (m, v) -> m.put(keyMapping.apply(v), valueMapping.apply(v)), Collections.emptyMap(), Function.identity());
	}

	/**
	 * Executes the query and returns the result as a {@link Set}.
	 *
	 * @return A set of entities representing the result rows.
	 */
	@Override
	public Set<R> toSet() {
		return resultHandling(new HashSet<>(), Set::add, Collections.emptySet(), Function.identity());
	}

	@Override
	public String getQuery() {
		return this.queryBuilder.build(this.queryParameters);
	}

	/**
	 * Performs handling on a {@code ResultSet} for different data structures.
	 *
	 * @param dataStructure The data structure to fill the values from the {@code ResultSet} with.
	 * @param valueConsumer The action to perform with a retrieved value from the {@code ResultSet}.
	 * @param defaultValue  A default value to be used in case an exception occurs.
	 * @param valueMapping  A mapping to convert the dataStructure into the desired return type.
	 * @param <T>           The type of the data structure which will be returned.
	 * @param <D>           The type of the initial data structure which the {@code ResultSet} will work with.
	 * @return A data structure containing a {@code ResultSet}s data.
	 */
	private <T, D> T resultHandling(D dataStructure, BiConsumer<D, R> valueConsumer, T defaultValue, Function<D, T> valueMapping) {
		try (var connection = new DBConnection(this.connectionConfiguration);
			 var result = connection.execute(getQuery())) {
			while (result.next()) {
				valueConsumer.accept(dataStructure, result.getObject(1, this.returnType));
			}

			return valueMapping.apply(dataStructure);
		} catch (SQLException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}
}
