package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseDeletableEntity;
import com.github.collinalpert.java2db.utilities.Utilities;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Describes a service class for an entity which contains an id and an isDeleted flag.
 * Note that when deleting entities which have an isDeleted flag with this service,
 * they will not actually be deleted from the database, but the flag will be set to {@code true}.
 *
 * @author Collin Alpert
 */
public class BaseDeletableService<T extends BaseDeletableEntity> extends BaseService<T> {

	@Override
	public void delete(T instance) throws SQLException {
		this.delete(instance.getId());
	}

	@Override
	public void delete(long id) throws SQLException {
		super.update(id, BaseDeletableEntity::isDeleted, true);
	}

	@Override
	public void delete(List<T> entities) throws SQLException {
		var joiner = new StringJoiner(", ", "(", ")");
		for (T entity : entities) {
			joiner.add(Long.toString(entity.getId()));
		}

		var joinedIds = joiner.toString();
		SqlFunction<T, ?> deletedFunc = BaseDeletableEntity::isDeleted;
		try (var connection = new DBConnection()) {
			connection.update(String.format("update %s set %s = 1 where %s.`id` in %s", this.tableName, Lambda2Sql.toSql(deletedFunc, this.tableName), this.tableName, joinedIds));
			Utilities.logf("%s with ids %s successfully soft deleted!", this.type.getSimpleName(), joinedIds);
		}
	}

	@Override
	public void delete(T... entities) throws SQLException {
		this.delete(Arrays.asList(entities));
	}

	@Override
	public void delete(long... ids) throws SQLException {
		var list = new ArrayList<Long>(ids.length);
		for (long id : ids) {
			list.add(id);
		}

		this.delete(x -> list.contains(x.getId()));
	}

	@Override
	public void delete(SqlPredicate<T> predicate) throws SQLException {
		SqlFunction<T, Boolean> isDeletedFunction = BaseDeletableEntity::isDeleted;
		var query = String.format("update %s set %s.`%s` = 1 where %s", super.tableName, super.tableName, Lambda2Sql.toSql(isDeletedFunction), Lambda2Sql.toSql(predicate));
		try (var connection = new DBConnection()) {
			connection.update(query);
			Utilities.logf("%s successfully soft deleted!", this.type.getSimpleName());
		}
	}
}
