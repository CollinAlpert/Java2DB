package de.java2db.mappers;

import de.java2db.database.ForeignKey;
import de.java2db.database.ForeignKeyObject;
import de.java2db.entities.BaseEntity;
import de.java2db.utilities.EmptyResultSetException;
import de.java2db.utilities.IoC;
import de.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Collin Alpert
 * <p>
 * Default mapper for converting a {@link ResultSet} to the respective Java entity.
 * </p>
 */
public class BaseMapper<T extends BaseEntity> {

	private Class<T> clazz;

	public BaseMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Maps a {@link ResultSet} with a single row to a Java entity.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A Java entity.
	 * @throws EmptyResultSetException if the {@link ResultSet} is empty.
	 *                                 It is expected that a query that is supposed to return exactly one value, actually does.
	 */
	public T map(ResultSet set) {
		T entity = IoC.resolve(clazz);
		try {
			if (set.next()) {
				setFields(set, entity);
			} else {
				if (isResultSetEmpty(set))
					throw new EmptyResultSetException(String.format("No entry found for query of type %s.", clazz.getSimpleName()));
			}
			set.close();
			return entity;
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to a list of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A list of Java entities.
	 */
	public List<T> mapToList(ResultSet set) {
		List<T> list = new ArrayList<>();
		try {
			while (set.next()) {
				T entity = IoC.resolve(clazz);
				setFields(set, entity);
				list.add(entity);
			}
			set.close();
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Sets the corresponding fields in an entity based on a {@link ResultSet}.
	 * If a field is marked as a foreign key object, a new query is started to fill this entity with a value.
	 *
	 * @param set    The {@link ResultSet} to get the data from.
	 * @param entity The Java entity to fill.
	 * @throws IllegalAccessException if there is an error in the accessibility of the entity.
	 */
	private void setFields(ResultSet set, T entity) throws IllegalAccessException {
		var fields = Utilities.getAllFields(entity, true);
		try {
			var foreignKeys = getForeignKeys(fields, set);
			var foreignKeyObjects = getForeignKeyObjects(fields);
			for (var field : fields) {
				field.setAccessible(true);
				if (field.getAnnotation(ForeignKeyObject.class) != null) {
					var fkObject = field.getAnnotation(ForeignKeyObject.class);
					Class<?> clz = foreignKeyObjects.keySet().stream().filter(x -> x == field.getType()).findFirst().orElseThrow();
					var service = IoC.resolveServiceByEntity((Class<? extends BaseEntity>) clz);
					field.set(entity, service.getById(foreignKeys.get(fkObject.value())));
					continue;
				}
				var value = set.getObject(field.getName(), field.getType());
				field.set(entity, value);

			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Cannot use base mapping. Please define custom mapping in according mapping class.");
		}
	}

	/**
	 * @param fields List of fields to get the foreign key annotations from.
	 * @param set    The {@link ResultSet} is needed to get the actual foreign key.
	 * @return A {@link Map} where the keys are the id number of this foreign key and the values are the actual foreign keys.
	 * @throws SQLException when the foreign key in the {@link ResultSet} does not exist.
	 */
	private Map<Integer, Integer> getForeignKeys(ArrayList<Field> fields, ResultSet set) throws SQLException {
		Map<Integer, Integer> map = new HashMap<>();
		ForeignKey key;
		for (Field field : fields) {
			if ((key = field.getAnnotation(ForeignKey.class)) != null) {
				map.put(key.value(), set.getInt(field.getName()));
			}
		}
		return map;
	}

	/**
	 * Gets the classes of all fields that represent a foreign key object, i.e. do not exist on the database.
	 *
	 * @param fields A list of fields to get the foreign key objects from.
	 * @return A {@link Map} where the keys are the classes of the foreign keys and the value is the id number for this foreign key.
	 */
	private Map<Class<?>, Integer> getForeignKeyObjects(ArrayList<Field> fields) {
		return fields.stream().filter(field -> field.getAnnotation(ForeignKeyObject.class) != null)
				.collect(Collectors.toMap(Field::getType, field -> field.getAnnotation(ForeignKeyObject.class).value()));
	}

	/**
	 * Checks if a {@link ResultSet} is empty.
	 *
	 * @param set The {@link ResultSet} to check.
	 * @return <code>True</code> if the {@link ResultSet} is empty, <code>false</code> if not.
	 */
	private boolean isResultSetEmpty(ResultSet set) {
		try {
			return !set.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
}
