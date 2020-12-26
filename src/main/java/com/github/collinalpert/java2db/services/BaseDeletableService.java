package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseDeletableEntity;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Describes a service class for an entity which contains an id and an isDeleted flag.
 * Note that when deleting entities which have an isDeleted flag with this service,
 * they will not actually be deleted from the database, but the flag will be set to {@code true}.
 *
 * @author Collin Alpert
 */
public class BaseDeletableService<T extends BaseDeletableEntity> extends BaseService<T> {

	private final SqlFunction<T, Boolean> isDeletedFunc = BaseDeletableEntity::isDeleted;

	protected BaseDeletableService(ConnectionConfiguration connectionConfiguration) {
		super(connectionConfiguration);
	}

	/**
	 * Performs a soft delete on an entity instead of completely deleting it from the database.
	 *
	 * @param instance The instance to soft-delete on the database.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@Override
	public void delete(T instance) throws SQLException {
		this.delete(instance.getId());
	}

	/**
	 * Performs a soft delete on an entity instead of completely deleting it from the database.
	 *
	 * @param id The row with this id to soft-delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@Override
	public void delete(int id) throws SQLException {
		super.update(id, this.isDeletedFunc, true);
	}

	/**
	 * Performs a soft delete on an list of entities instead of completely deleting them from the database.
	 *
	 * @param entities The list of entities to soft-delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@Override
	public void delete(List<T> entities) throws SQLException {
		var joiner = new StringJoiner(", ", "(", ")");
		for (T entity : entities) {
			joiner.add(Integer.toString(entity.getId()));
		}

		var joinedIds = joiner.toString();
		try (var connection = new DBConnection(super.connectionConfiguration)) {
			connection.update(String.format("update `%s` set %s = 1 where `%s`.`id` in %s", this.tableName, Lambda2Sql.toSql(this.isDeletedFunc, this.tableName), this.tableName, joinedIds));
		}
	}

	/**
	 * Performs a soft delete on an variable amount of entities instead of completely deleting them from the database.
	 *
	 * @param entities A variable amount of entities to soft-delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@SafeVarargs
	@Override
	public final void delete(T... entities) throws SQLException {
		this.delete(Arrays.asList(entities));
	}

	/**
	 * Performs a soft delete on a variable amount of entities instead of completely deleting them from the database.
	 *
	 * @param ids The ids to soft-delete the rows by.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@Override
	public void delete(int... ids) throws SQLException {
		var list = new ArrayList<Integer>(ids.length);
		for (var id : ids) {
			list.add(id);
		}

		this.delete(x -> list.contains(x.getId()));
	}

	/**
	 * Performs a soft delete based on a condition instead of completely deleting entities from the database.
	 *
	 * @param predicate The condition to soft-delete by.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	@Override
	public void delete(SqlPredicate<T> predicate) throws SQLException {
		var query = String.format("update %s set %s = 1 where %s", super.tableName, Lambda2Sql.toSql(this.isDeletedFunc, super.tableName), Lambda2Sql.toSql(predicate));
		try (var connection = new DBConnection(super.connectionConfiguration)) {
			connection.update(query);
		}
	}
}
