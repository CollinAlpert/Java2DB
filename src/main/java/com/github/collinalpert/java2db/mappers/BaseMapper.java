package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.UniqueIdentifier;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
public class BaseMapper<T extends BaseEntity> implements Mapper<T> {

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
		T entity = IoC.resolve(clazz);
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
		var list = new ArrayList<T>();
		while (set.next()) {
			var entity = IoC.resolve(clazz);
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
			var entity = IoC.resolve(clazz);
			setFields(set, entity);
			stream = Stream.concat(stream, Stream.of(entity));
		}

		set.close();
		UniqueIdentifier.unset();
		return stream;
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
		var tableName = Utilities.getTableName(entity.getClass());
		var foreignKeyFields = new LinkedList<Field>();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getAnnotation(ForeignKeyEntity.class) != null) {
					if (!BaseEntity.class.isAssignableFrom(field.getType())) {
						throw new IllegalArgumentException(String.format("Type %s which is annotated as a foreign key, does not extend BaseEntity", field.getType().getSimpleName()));
					}

					foreignKeyFields.add(field);
					var foreignKeyObject = IoC.resolve((Class<? extends BaseEntity>) field.getType());
					setFields(set, foreignKeyObject, UniqueIdentifier.getIdentifier(field.getName()));
					field.set(entity, foreignKeyObject);
					continue;
				}

				var columnLabel = (identifier == null ? tableName : identifier) + "_" + field.getName();
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

				field.set(entity, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		validateEntityForNull(entity, foreignKeyFields);
	}

	/**
	 * Sets all foreign key entities to {@code null} where the foreign key is also null, since this cannot be prevented while filling the entity.
	 *
	 * @param entity           The entity to be validated.
	 * @param foreignKeyFields The existing foreign key fields in this entity to be checked.
	 * @param <E>              The type of the entity.
	 */
	private <E extends BaseEntity> void validateEntityForNull(E entity, List<Field> foreignKeyFields) {
		foreignKeyFields.forEach(field -> {
			var foreignKeyText = field.getAnnotation(ForeignKeyEntity.class).value();
			try {
				field.setAccessible(true);
				var foreignKeyField = entity.getClass().getDeclaredField(foreignKeyText);
				foreignKeyField.setAccessible(true);
				var value = foreignKeyField.get(entity);
				if (value == null) {
					field.set(entity, null);
				}

			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
}
