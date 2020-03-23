package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.annotations.ColumnName;
import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.annotations.ForeignKeyPath;
import com.github.collinalpert.java2db.contracts.IdentifiableEnum;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.AnnotationModule;
import com.github.collinalpert.java2db.modules.ArrayModule;
import com.github.collinalpert.java2db.modules.FieldModule;
import com.github.collinalpert.java2db.modules.TableModule;
import com.github.collinalpert.java2db.utilities.IoC;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.collinalpert.java2db.utilities.Utilities.tryAction;
import static com.github.collinalpert.java2db.utilities.Utilities.tryGetValue;

/**
 * Default mapper for converting a {@link ResultSet} to the respective Java entity.
 *
 * @author Collin Alpert
 */
public class EntityMapper<E extends BaseEntity> implements Mappable<E> {

	private static final TableModule tableModule;

	static {
		tableModule = TableModule.getInstance();
	}

	private Class<E> clazz;
	private final Map<String, String> aliases;

	public EntityMapper(Class<E> clazz) {
		this.clazz = clazz;
		this.aliases = FieldModule.getInstance().getAliases(clazz);
	}

	/**
	 * Maps a {@link ResultSet} with a single row to a Java entity.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return An Optional which contains the Java entity if the query was successful.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected or if the entity fields cannot be set.
	 */
	@Override
	public Optional<E> map(ResultSet set) throws SQLException {
		E entity = IoC.createInstance(this.clazz);
		if (!set.next()) {
			return Optional.empty();
		}

		setFields(set, entity);
		set.close();
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
	public List<E> mapToList(ResultSet set) throws SQLException {
		var list = new ArrayList<E>();
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
	public Stream<E> mapToStream(ResultSet set) throws SQLException {
		var builder = Stream.<E>builder();
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
	public E[] mapToArray(ResultSet set) throws SQLException {
		var module = new ArrayModule<>(this.clazz, 20);
		mapInternal(set, module::addElement);
		return module.getArray();
	}

	/**
	 * Maps a {@code ResultSet} to a {@link Map}.
	 *
	 * @param set          The {@code ResultSet} to get the data from.
	 * @param keyMapping   The key function of the map.
	 * @param valueMapping The value function of the map.
	 * @param <K>          The type of the keys in the map.
	 * @param <V>          The type of the values in the map.
	 * @return A {@code Map} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	@Override
	public <K, V> Map<K, V> mapToMap(ResultSet set, Function<E, K> keyMapping, Function<E, V> valueMapping) throws SQLException {
		var map = new HashMap<K, V>();
		mapInternal(set, x -> map.put(keyMapping.apply(x), valueMapping.apply(x)));
		return map;
	}

	/**
	 * Internal handling for executing a certain action for every entity that is generated when iterating through a {@code ResultSet}.
	 *
	 * @param set      The {@code ResultSet} which will be iterated through.
	 * @param handling The action to apply at each iteration of the given {@code ResultSet}.
	 * @throws SQLException Handling a {@code ResultSet} can possibly result in this exception being thrown.
	 */
	private void mapInternal(ResultSet set, Consumer<E> handling) throws SQLException {
		while (set.next()) {
			var entity = IoC.createInstance(this.clazz);
			setFields(set, entity);
			handling.accept(entity);
		}

		set.close();
	}

	/**
	 * Fills the corresponding fields in an entity based on a {@link ResultSet}.
	 *
	 * @param set    The {@link ResultSet} to get the data from.
	 * @param entity The Java entity to fill.
	 */
	private <TEntity extends BaseEntity> void setFields(ResultSet set, TEntity entity) throws SQLException {
		setFields(set, entity, tableModule.getTableName(entity.getClass()));
	}

	/**
	 * Fills the corresponding fields in an entity based on a {@link ResultSet}.
	 *
	 * @param set        The {@link ResultSet} to get the data from.
	 * @param identifier The alias set for a certain entity used as a nested property.
	 * @param entity     The Java entity to fill.
	 */
	private <TEntity extends BaseEntity> void setFields(ResultSet set, TEntity entity, String identifier) throws SQLException {
		var fieldModule = FieldModule.getInstance();
		var fields = fieldModule.getEntityFields(entity.getClass(), true);
		for (var field : fields) {
			field.setAccessible(true);

			if (field.getAnnotation(ForeignKeyEntity.class) != null) {

				if (field.getType().isEnum()) {
					if (!IdentifiableEnum.class.isAssignableFrom(field.getType())) {
						throw new IllegalArgumentException(String.format("The enum %s used in %s was annotated with a ForeignKeyEntity attribute but does not extend IdentifiableEnum.", field.getType().getSimpleName(), field.getDeclaringClass().getSimpleName()));
					}

					var foreignKeyName = getForeignKeyName(field);
					var value = tryGetValue(() -> getAccessibleField(field.getDeclaringClass(), foreignKeyName).get(entity));
					if (value == null) {
						continue;
					}

					var foundEnum = Arrays.stream(field.getType().getEnumConstants())
							.map(x -> (IdentifiableEnum) x)
							.filter(x -> Long.parseLong(value.toString()) == x.getId())
							.findFirst();

					foundEnum.ifPresent(identifiableEnum -> tryAction(() -> field.set(entity, field.getType().cast(identifiableEnum))));

					continue;
				}

				var foreignKeyPathInfo = AnnotationModule.getInstance().getAnnotationInfo(field, ForeignKeyPath.class);
				if (foreignKeyPathInfo.hasAnnotation()) {
					var aliasKey = String.join("_", identifier, field.getName());
					tryAction(() -> field.set(entity, set.getObject(this.aliases.get(aliasKey) + "_" + foreignKeyPathInfo.getAnnotation().value(), field.getType())));

					continue;
				}

				if (!BaseEntity.class.isAssignableFrom(field.getType())) {
					throw new IllegalArgumentException(String.format("Type %s, which is annotated as a foreign key, does not extend BaseEntity.", field.getType().getSimpleName()));
				}

				// If foreign key is null, the corresponding entity must also be null.
				if (set.getObject(String.join("_", identifier, getForeignKeyName(field))) == null) {
					continue;
				}

				@SuppressWarnings("unchecked")
				var foreignKeyObject = IoC.createInstance((Class<? extends BaseEntity>) field.getType());
				setFields(set, foreignKeyObject, this.aliases.get(field.getDeclaringClass().getSimpleName().toLowerCase() + "_" + field.getName()));
				tryAction(() -> field.set(entity, foreignKeyObject));

				continue;
			}

			var columnLabel = String.join("_", identifier, tableModule.getColumnName(field));

			Object value = getValue(set, columnLabel, field.getType());

			if (value == null) {
				continue;
			}

			tryAction(() -> field.set(entity, value));
		}
	}

	/**
	 * Get's a value from a {@code ResultSet} while performing some additional null checks.
	 *
	 * @param set         The {@code ResultSet} to retrieve the value from.
	 * @param columnLabel The name of the column which holds the value.
	 * @param type        The type of the value to be fetched.
	 * @return An object containing the retrieved value.
	 * @throws SQLException In case no value with the given name was found in the {@code ResultSet}.
	 */
	private Object getValue(ResultSet set, String columnLabel, Class<?> type) throws SQLException {
		if (type == LocalDateTime.class) {
			var value = set.getTimestamp(columnLabel, Calendar.getInstance(Locale.getDefault()));
			return value == null ? null : value.toLocalDateTime();
		} else if (type == LocalDate.class) {
			var value = set.getDate(columnLabel, Calendar.getInstance(Locale.getDefault()));
			return value == null ? null : value.toLocalDate();
		} else if (type == LocalTime.class) {
			var value = set.getTime(columnLabel, Calendar.getInstance(Locale.getDefault()));
			return value == null ? null : value.toLocalTime();
		} else {
			return set.getObject(columnLabel);
		}
	}

	/**
	 * Get's the name of a foreign key column in a table from a {@code Field} marked with the {@link ForeignKeyEntity} annotation.
	 *
	 * @param field The field to get the foreign key information from.
	 * @return The name of the column which has the foreign key constraint.
	 */
	private String getForeignKeyName(Field field) {
		var annotationModule = AnnotationModule.getInstance();
		var foreignKeyColumnName = field.getAnnotation(ForeignKeyEntity.class).value();

		try {
			var foreignKeyField = field.getDeclaringClass().getDeclaredField(foreignKeyColumnName);
			return tableModule.getColumnName(foreignKeyField);
		} catch (NoSuchFieldException e) {
			//Oh boi, you've done it now! This case occurs when a foreign key field gets altered by a ColumnName attribute.
			for (var declaredField : field.getDeclaringClass().getDeclaredFields()) {
				var info = annotationModule.getAnnotationInfo(declaredField, ColumnName.class, a -> a.value().equals(foreignKeyColumnName));
				if (info.hasAnnotation()) {
					return info.getAnnotation().value();
				}

			}
		}

		return "";
	}

	/**
	 * Retrieves any field (including private ones) from a class by its name and makes it accessible.
	 *
	 * @param clazz The class to get the field from.
	 * @param name  The name of the field.
	 * @return The field which has been made accessible.
	 * @throws NoSuchFieldException If no field with a matching name exists in the given class.
	 */
	private Field getAccessibleField(Class<?> clazz, String name) throws NoSuchFieldException {
		var field = clazz.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}
