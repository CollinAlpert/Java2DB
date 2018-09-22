package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.database.ForeignKey;
import com.github.collinalpert.java2db.database.ForeignKeyObject;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	 * @return An Optional which contains the Java entity if the query was successful.
	 */
	public Optional<T> map(ResultSet set) {
		T entity = IoC.resolve(clazz);
		try {
			if (set.next()) {
				setFields(set, entity);
			} else {
				set.close();
				return Optional.empty();
			}
			set.close();
			return Optional.of(entity);
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to a list of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A list of Java entities.
	 */
	public List<T> mapToList(ResultSet set) {
		var list = new ArrayList<T>();
		try {
			while (set.next()) {
				var entity = IoC.resolve(clazz);
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
			for (var field : fields) {
				field.setAccessible(true);
				ForeignKeyObject foreignKeyObject;
				if ((foreignKeyObject = field.getAnnotation(ForeignKeyObject.class)) != null) {
					if (!BaseEntity.class.isAssignableFrom(field.getType())) {
						throw new IllegalArgumentException(String.format("Foreign key object %s with id %d does not extend BaseEntity.", field.getType(), foreignKeyObject.value()));
					}
					var service = IoC.resolveServiceByEntity((Class<? extends BaseEntity>) field.getType());
					var optionalEntity = service.getById(foreignKeys.get(foreignKeyObject.value()));
					if (!optionalEntity.isPresent()) {
						System.err.printf("Could not set type %s with name %s\n", field.getType(), field.getName());
						continue;
					}
					field.set(entity, optionalEntity.get());
					continue;
				}
				var value = set.getObject(field.getName(), field.getType());
				field.set(entity, value);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			//TODO add possibility of custom mapping classes.
			throw new IllegalArgumentException("Cannot use base mapping. Please define custom mapping in according mapping class.");
		}
	}


	/**
	 * @param fields Map of fields to get the foreign key annotations from.
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
}
