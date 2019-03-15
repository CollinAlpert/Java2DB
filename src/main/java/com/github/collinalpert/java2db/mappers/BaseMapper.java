package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.annotations.ColumnName;
import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.ArrayModule;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.UniqueIdentifier;
import com.github.collinalpert.java2db.utilities.Utilities;

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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.collinalpert.java2db.utilities.Utilities.tryAction;

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
		mapInternal(set, list::add);
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
		var builder = Stream.<T>builder();
		mapInternal(set, builder::add);
		return builder.build();
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
		var module = new ArrayModule<>(this.clazz, 20);
		mapInternal(set, module::addElement);
		return module.getArray();
	}

	/**
	 * Internal handling for executing a certain action for every entity that is generated when iterating through a {@code ResultSet}.
	 *
	 * @param set      The {@code ResultSet} which will be iterated through.
	 * @param handling The action to apply at each iteration of the given {@code ResultSet}.
	 * @throws SQLException Handling a {@code ResultSet} can possibly result in this exception being thrown.
	 */
	private void mapInternal(ResultSet set, Consumer<T> handling) throws SQLException {
		while (set.next()) {
			var entity = IoC.createInstance(this.clazz);
			setFields(set, entity);
			handling.accept(entity);
		}

		set.close();
		UniqueIdentifier.unset();
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
		for (var field : fields) {
			field.setAccessible(true);
			if (field.getAnnotation(ForeignKeyEntity.class) != null) {
				var foreignKeyColumnName = field.getAnnotation(ForeignKeyEntity.class).value();
				if (!BaseEntity.class.isAssignableFrom(field.getType())) {
					throw new IllegalArgumentException(String.format("Type %s, which is annotated as a foreign key, does not extend BaseEntity.", field.getType().getSimpleName()));
				}

				// This block is for checking if the foreign key is null.
				// That means that the corresponding foreign key entity must be set to null.
				var foreignKeyName = "";
				try {
					var foreignKeyField = field.getDeclaringClass().getDeclaredField(foreignKeyColumnName);
					foreignKeyName = foreignKeyField.getName();
					if (foreignKeyField.getAnnotation(ColumnName.class) != null) {
						foreignKeyName = foreignKeyField.getAnnotation(ColumnName.class).value();
					}
				} catch (NoSuchFieldException e) {
					//Oh boi, you've done it now!
					for (var declaredField : field.getDeclaringClass().getDeclaredFields()) {
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
				tryAction(() -> field.set(entity, foreignKeyObject));

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

			tryAction(() -> field.set(entity, value));
		}
	}
}
