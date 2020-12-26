package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.modules.*;
import com.github.collinalpert.java2db.utilities.IoC;

import java.sql.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import static com.github.collinalpert.java2db.utilities.Utilities.tryAction;

/**
 * Maps a {@link ResultSet} into fields of an object or into simple types.
 *
 * @author Collin Alpert
 */
public class FieldMapper<T> implements Mappable<T> {

	private static final Set<Class<?>> simpleValueTypes;

	static {
		simpleValueTypes = new HashSet<>();
		simpleValueTypes.add(String.class);
		simpleValueTypes.add(Byte.class);
		simpleValueTypes.add(byte.class);
		simpleValueTypes.add(Short.class);
		simpleValueTypes.add(short.class);
		simpleValueTypes.add(Integer.class);
		simpleValueTypes.add(int.class);
		simpleValueTypes.add(Long.class);
		simpleValueTypes.add(long.class);
		simpleValueTypes.add(Double.class);
		simpleValueTypes.add(double.class);
		simpleValueTypes.add(Float.class);
		simpleValueTypes.add(float.class);
		simpleValueTypes.add(Boolean.class);
		simpleValueTypes.add(boolean.class);
		simpleValueTypes.add(Character.class);
		simpleValueTypes.add(char.class);
	}

	private final Class<T> underlyingClass;

	public FieldMapper(Class<T> underlyingClass) {
		this.underlyingClass = underlyingClass;
	}

	/**
	 * Maps a {@code ResultSet} to an {@code Optional}. Should be used when only one result is expected from the database.
	 * If any column cannot be set, a {@link RuntimeException} is thrown.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	@Override
	public Optional<T> map(ResultSet set) throws SQLException {
		if (simpleValueTypes.contains(this.underlyingClass)) {
			if (set.next()) {
				return Optional.ofNullable(set.getObject(1, this.underlyingClass));
			} else {
				set.close();
				return Optional.empty();
			}
		}

		var instance = IoC.createInstance(this.underlyingClass);
		var fields = FieldModule.getInstance().getAllFields(this.underlyingClass);
		if (set.next()) {
			for (var field : fields) {
				field.setAccessible(true);
				tryAction(() -> field.set(instance, set.getObject(field.getName(), field.getType())));
			}

			set.close();
			return Optional.of(instance);
		}

		set.close();

		return Optional.empty();
	}

	/**
	 * Maps a {@code ResultSet} to a {@code List}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code List} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 * @see #mapInternal(ResultSet, Object, BiConsumer, Function)
	 */
	@Override
	public List<T> mapToList(ResultSet set) throws SQLException {
		return mapInternal(set, new LinkedList<>(), List::add, Function.identity());
	}

	/**
	 * Maps a {@code ResultSet} to a {@code Stream}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code Stream} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 * @see #mapInternal(ResultSet, Object, BiConsumer, Function)
	 */
	@Override
	public Stream<T> mapToStream(ResultSet set) throws SQLException {
		return mapInternal(set, Stream.<T>builder(), Stream.Builder::accept, Stream.Builder::build);
	}

	/**
	 * Maps a {@code ResultSet} to an array.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return An array containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 * @see #mapInternal(ResultSet, Object, BiConsumer, Function)
	 */
	@Override
	public T[] mapToArray(ResultSet set) throws SQLException {
		return mapInternal(set, new ArrayModule<>(this.underlyingClass, 5), ArrayModule::addElement, ArrayModule::getArray);
	}

	/**
	 * Maps a {@code ResultSet} to a {@link Map}.
	 *
	 * @param set          The {@code ResultSet} to get the data from.
	 * @param keyMapping   The key function of the map.
	 * @param valueMapping The value function of the map.
	 * @return A {@code Map} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	@Override
	public <K, V> Map<K, V> mapToMap(ResultSet set, Function<T, K> keyMapping, Function<T, V> valueMapping) throws SQLException {
		return mapInternal(set, new HashMap<>(), (m, e) -> m.put(keyMapping.apply(e), valueMapping.apply(e)), Function.identity());
	}

	/**
	 * Maps a {@code ResultSet} to a {@code Set}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code Set} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	@Override
	public Set<T> mapToSet(ResultSet set) throws SQLException {
		return mapInternal(set, new HashSet<>(), Set::add, Function.identity());
	}

	/**
	 * Internal template method which maps a {@link ResultSet} to a given container which holds {@link T}s.
	 *
	 * @param set            The ResultSet to map.
	 * @param container      The container which holds the values.
	 * @param addFunction    A function which applies elements of type T to the container.
	 * @param returnFunction A function which brings the container in the desired form so it can be returned.
	 * @param <R>            The form of the container when it is ready to be returned.
	 * @param <O>            The initial state of the container in which elements can be applied to it.
	 * @return A container in the desired form as defined in the {@param returnFunction} Parameter.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	private <R, O> R mapInternal(ResultSet set, O container, BiConsumer<O, T> addFunction, Function<O, R> returnFunction) throws SQLException {
		if (simpleValueTypes.contains(this.underlyingClass)) {
			while (set.next()) {
				addFunction.accept(container, set.getObject(1, this.underlyingClass));
			}

			return returnFunction.apply(container);
		}

		while (set.next()) {
			var instance = IoC.createInstance(this.underlyingClass);
			for (var field : FieldModule.getInstance().getAllFields(this.underlyingClass)) {
				field.setAccessible(true);
				tryAction(() -> field.set(instance, set.getObject(field.getName(), field.getType())));
			}

			addFunction.accept(container, instance);
		}

		set.close();

		return returnFunction.apply(container);
	}
}
