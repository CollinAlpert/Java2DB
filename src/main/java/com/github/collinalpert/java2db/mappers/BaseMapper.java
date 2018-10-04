package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.annotations.ForeignKeyObject;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default mapper for converting a {@link ResultSet} to the respective Java entity.
 *
 * @author Collin Alpert
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
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected.
	 */
	public Optional<T> map(ResultSet set) throws SQLException {
		T entity = IoC.resolve(clazz);
		try (set) {
			if (set.next()) {
				setFields(set, entity);
			} else {
				return Optional.empty();
			}
			return Optional.of(entity);
		}
	}

	/**
	 * Maps a {@link ResultSet} with multiple rows to a list of Java entities.
	 *
	 * @param set The {@link ResultSet} to map.
	 * @return A list of Java entities.
	 * @throws SQLException if the {@link ResultSet#next()} call does not work as expected.
	 */
	public List<T> mapToList(ResultSet set) throws SQLException {
		var list = new ArrayList<T>();
		try (set) {
			while (set.next()) {
				var entity = IoC.resolve(clazz);
				setFields(set, entity);
				list.add(entity);
			}
		}
		return list;
	}

	/**
	 * Fills the corresponding fields in an entity based on a {@link ResultSet}.
	 * If a field is marked as a foreign key object, a new query is started to fill this entity with a value.
	 *
	 * @param set    The {@link ResultSet} to get the data from.
	 * @param entity The Java entity to fill.
	 */
	private <E extends BaseEntity> void setFields(ResultSet set, E entity) throws SQLException {
		var fields = Utilities.getEntityFields(entity.getClass(), true);
		var tableName = Utilities.getTableName(entity.getClass());
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getAnnotation(ForeignKeyObject.class) != null) {
					if (!BaseEntity.class.isAssignableFrom(field.getType())) {
						throw new IllegalArgumentException(String.format("Type %s which is annotated as a foreign key, does not extend BaseEntity", field.getType().getSimpleName()));
					}
					var foreignKeyObject = IoC.resolve((Class<? extends BaseEntity>) field.getType());
					setFields(set, foreignKeyObject);
					field.set(entity, foreignKeyObject);
					continue;
				}
				var value = set.getObject(tableName + "_" + field.getName(), field.getType());
				field.set(entity, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
