package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.annotations.ColumnName;
import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.UniqueIdentifier;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default mapper for converting a {@link ResultSet} to the respective Java entity.
 *
 * @author Collin Alpert
 */
public class BaseMapper<T extends BaseEntity> implements IMapper<T> {

	private Class<T> clazz;

	public BaseMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Maps a {@link ResultSet} with a single row to a Java entity.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return An Optional which contains the Java entity if the query was successful.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected or if the entity fields cannot be set.
	 */
	@Override
	public Optional<T> map(ResultSet set) throws SQLException {
		T entity = IoC.createInstance(this.clazz);
		if (!set.next()) {
			UniqueIdentifier.unset();
			return Optional.empty();
		}

		setFields(set, entity);
		set.close();
		UniqueIdentifier.unset();
		return Optional.of(entity);
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to a list of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A list of Java entities.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected or if the entity fields cannot be set.
	 */
	@Override
	public List<T> mapToList(ResultSet set) throws SQLException {
		var list = new LinkedList<T>();
		while (set.next()) {
			var entity = IoC.createInstance(this.clazz);
			setFields(set, entity);
			list.add(entity);
		}

		set.close();
		UniqueIdentifier.unset();
		return list;
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to a {@code Stream} of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A {@code Stream} of Java entities.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected or if the entity fields cannot be set.
	 */
	@Override
	public Stream<T> mapToStream(ResultSet set) throws SQLException {
		Stream<T> stream = Stream.empty();
		while (set.next()) {
			var entity = IoC.createInstance(this.clazz);
			setFields(set, entity);
			stream = Stream.concat(stream, Stream.of(entity));
		}

		set.close();
		UniqueIdentifier.unset();
		return stream;
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to an array of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return An array of Java entities.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected or if the entity fields cannot be set.
	 */
	@Override
	public T[] mapToArray(ResultSet set) throws SQLException {
		@SuppressWarnings("unchecked")
		var array = (T[]) Array.newInstance(this.clazz, 20);
		var index = 0;
		while (set.next()) {
			if (array.length - 1 == index) {
				array = createCopy(array, array.length + 20, array.length);
			}

			var entity = IoC.createInstance(this.clazz);
			setFields(set, entity);
			array[index++] = entity;
		}

		UniqueIdentifier.unset();
		return trimArray(array);
	}

	/**
	 * Trims an array, removing all unnecessary slots. Unnecessary slots are those that are {@code null}.
	 *
	 * @param array The array to trim.
	 * @return A new array that has the size of it's containing elements.
	 */
	private T[] trimArray(T[] array) {
		var lastElement = 0;
		for (var i = array.length - 1; i >= 0; i--) {
			if (array[i] != null) {
				lastElement = i;
				break;
			}
		}

		return createCopy(array, lastElement + 1, lastElement + 1);
	}

	/**
	 * Copies an array into an array with a new length.
	 *
	 * @param oldArray       The array to copy.
	 * @param newLength      The new length of the array.
	 * @param elementsToCopy The number of elements to copy to the new array.
	 * @return A new array containing the old elements.
	 */
	private T[] createCopy(T[] oldArray, int newLength, int elementsToCopy) {
		@SuppressWarnings("unchecked")
		var newArray = (T[]) Array.newInstance(this.clazz, newLength);
		System.arraycopy(oldArray, 0, newArray, 0, elementsToCopy);
		return newArray;
	}

	/**
	 * Fills the corresponding fields in an entity based on a {@link ResultSet}.
	 *
	 * @param set    The {@link ResultSet} to get the data from.
	 * @param entity The Java entity to fill.
	 */
	private <E extends BaseEntity> void setFields(ResultSet set, E entity) throws SQLException {
		setFields(set, entity, null);
	}

	/**
	 * Fills the corresponding fields in an entity based on a {@link ResultSet}.
	 *
	 * @param set        The {@link ResultSet} to get the data from.
	 * @param identifier The alias set for a certain entity used as a nested property.
	 * @param entity     The Java entity to fill.
	 */
	private <E extends BaseEntity> void setFields(ResultSet set, E entity, String identifier) throws SQLException {
		var fields = Utilities.getEntityFields(entity.getClass(), true);
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getAnnotation(ForeignKeyEntity.class) != null) {
				var foreignKeyColumnName = field.getAnnotation(ForeignKeyEntity.class).value();
				if (!BaseEntity.class.isAssignableFrom(field.getType())) {
					throw new IllegalArgumentException(String.format("Type %s which is annotated as a foreign key, does not extend BaseEntity", field.getType().getSimpleName()));
				}

				// This block is for checking if the foreign key is null.
				// That means that the corresponding foreign key entity must be set to null.
				String foreignKeyName = "";
				try {
					var foreignKeyField = field.getDeclaringClass().getDeclaredField(foreignKeyColumnName);
					foreignKeyName = foreignKeyField.getName();
					if (foreignKeyField.getAnnotation(ColumnName.class) != null) {
						foreignKeyName = foreignKeyField.getAnnotation(ColumnName.class).value();
					}
				} catch (NoSuchFieldException e) {
					//Oh boi, you've done it now!
					for (Field declaredField : field.getDeclaringClass().getDeclaredFields()) {
						if (declaredField.getAnnotation(ColumnName.class) != null && declaredField.getAnnotation(ColumnName.class).value().equals(foreignKeyColumnName)) {
							foreignKeyName = declaredField.getAnnotation(ColumnName.class).value();
							break;
						}
					}
				}

				if (set.getObject((identifier == null ? Utilities.getTableName(entity.getClass()) : identifier) + "_" + foreignKeyName) == null) {
					continue;
				}

				@SuppressWarnings("unchecked")
				var foreignKeyObject = IoC.createInstance((Class<? extends BaseEntity>) field.getType());
				setFields(set, foreignKeyObject, UniqueIdentifier.getIdentifier(field.getName()));
				try {
					field.set(entity, foreignKeyObject);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				continue;
			}

			var columnName = Utilities.getColumnName(field);
			var columnLabel = (identifier == null ? Utilities.getTableName(entity.getClass()) : identifier) + "_" + columnName;

			Object value;
			if (field.getType() == LocalDateTime.class) {
				value = set.getTimestamp(columnLabel, Calendar.getInstance(Locale.getDefault())).toLocalDateTime();
			} else if (field.getType() == LocalDate.class) {
				value = set.getDate(columnLabel, Calendar.getInstance(Locale.getDefault())).toLocalDate();
			} else if (field.getType() == LocalTime.class) {
				value = set.getTime(columnLabel, Calendar.getInstance(Locale.getDefault())).toLocalTime();
			} else {
				value = set.getObject(columnLabel);
			}

			if (value == null) {
				continue;
			}

			try {
				field.set(entity, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
