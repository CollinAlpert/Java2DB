package de.java2db.services;

import de.java2db.database.DBConnection;
import de.java2db.database.TableName;
import de.java2db.entities.BaseEntity;
import de.java2db.mappers.BaseMapper;
import de.java2db.utilities.EmptyResultSetException;
import de.java2db.utilities.Lambda2Sql;
import de.java2db.utilities.SqlPredicate;
import de.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Collin Alpert
 */
public class BaseService<T extends BaseEntity> {

	private final String typeName;
	private final String tableName;

	private BaseMapper<T> mapper;

	protected BaseService(Class<T> clazz) {
		typeName = clazz.getSimpleName();
		tableName = clazz.getAnnotation(TableName.class).value();
		mapper = new BaseMapper<>(clazz);
	}

	//region Create
	public boolean create(T instance) {
		var insertQuery = new StringBuilder("insert into ").append(tableName).append(" (");
		var databaseFields = Utilities.getAllFields(instance).stream().map(Field::getName).collect(Collectors.joining(", "));
		insertQuery.append(databaseFields).append(") values (");
		Utilities.getAllFields(instance, BaseEntity.class).forEach(x -> {
			x.setAccessible(true);
			try {
				var value = x.get(instance);
				if (value == null) {
					insertQuery.append("default, ");
					return;
				}
				if (value instanceof String) {
					insertQuery.append("'").append(value).append("'").append(", ");
					return;
				}
				insertQuery.append(value).append(", ");
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to get value from field %s for type %s\n", x.getName(), typeName);
			}
		});
		insertQuery.append("default, default)");
		System.out.println(insertQuery.toString());
		try (var connection = new DBConnection()) {
			if (connection.update(insertQuery.toString())) {
				System.out.printf("%s successfully created!\n", typeName);
				return true;
			} else {
				System.err.printf("Unable to create type %s\n", typeName);
				return false;
			}
		}
	}
	//endregion

	//region Read

	protected ResultSet getByPredicate(SqlPredicate<T> predicate, DBConnection connection) {
		var query = "select * from " + tableName + " where " + Lambda2Sql.toSql(predicate);
		System.out.println(query);
		ResultSet set = connection.execute(query);
		if (isResultSetEmpty(set))
			throw new EmptyResultSetException(String.format("No entry found for query of type %s.", typeName));
		return set;
	}

	public T getSingle(SqlPredicate<T> predicate) {
		var connection = new DBConnection();
		var entity = mapper.map(getByPredicate(predicate, connection));
		connection.close();
		return entity;
	}

	public List<T> getMultiple(SqlPredicate<T> predicate) {
		var connection = new DBConnection();
		var entity = mapper.mapToList(getByPredicate(predicate, connection));
		connection.close();
		return entity;
	}

	public T getById(int id) {
		return getSingle(x -> x.getId() == id);
	}

	public List<T> getAll() {
		return getMultiple(x -> true);
	}
	//endregion

	//region Update
	public boolean update(T instance) {
		var updateQuery = new StringBuilder("update ").append(tableName).append(" set ");
		ArrayList<String> fieldSetterList = new ArrayList<>();
		Utilities.getAllFields(instance, BaseEntity.class).forEach(x -> {
			x.setAccessible(true);
			try {
				var value = x.get(instance);
				if (value instanceof String) {
					fieldSetterList.add(String.format("%s = '%s'", x.getName(), value));
					return;
				}
				fieldSetterList.add(x.getName() + " = " + value);
			} catch (IllegalAccessException e) {
				System.err.printf("Error getting value for field %s from type %s\n", x.getName(), typeName);
			}
		});
		updateQuery.append(String.join(", ", fieldSetterList))
				.append(" where id = ").append(instance.getId());
		System.out.println(updateQuery.toString());
		try (var connection = new DBConnection()) {
			if (connection.update(updateQuery.toString())) {
				System.out.printf("%s with id %d was successfully updated\n", typeName, instance.getId());
				return true;
			} else {
				System.err.printf("%s with id %d could not be updated\n", typeName, instance.getId());
				return false;
			}
		}
	}
	//endregion

	//region Delete
	public void delete(T instance) {
		delete(instance.getId());
	}

	public void delete(int id) {
		try (var connection = new DBConnection()) {
			boolean success = connection.update("delete from " + tableName + " where id=?", id);
			if (success) {
				System.out.printf("%s with id %d successfully deleted!\n", typeName, id);
			}
		}
	}
	//endregion

	private boolean isResultSetEmpty(ResultSet set) {
		try {
			return !set.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
}