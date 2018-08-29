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
 */
public class BaseMapper<T extends BaseEntity> {

	private Class<T> clazz;

	public BaseMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

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

	public List<T> mapToList(ResultSet set) {
		List<T> list = new ArrayList<>();
		try {
			while (set.next()) {
				T entity = IoC.resolve(clazz);
				setFields(set, entity);
				list.add(entity);
			}
			set.close();
			return list;
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

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

	private Map<Class<?>, Integer> getForeignKeyObjects(ArrayList<Field> fields) {
		return fields.stream().filter(field -> field.getAnnotation(ForeignKeyObject.class) != null)
				.collect(Collectors.toMap(Field::getType, field -> field.getAnnotation(ForeignKeyObject.class).value()));
	}

	private boolean isResultSetEmpty(ResultSet set) {
		try {
			return !set.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
}
