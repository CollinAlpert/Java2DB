package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.util.HashMap;
import java.util.Map;

/**
 * A class which administers default query constraints set for entities.
 *
 * @author Collin Alpert
 */
public class QueryConstraints {

	private static Map<Class<? extends BaseEntity>, SqlPredicate<? extends BaseEntity>> selectConstraints;

	static {
		selectConstraints = new HashMap<>();
	}

	/**
	 * Retrieves a composition {@link SqlPredicate} for all constraints that have been added for this class or any superclass of it.
	 *
	 * @param clazz The class to retrieve query constraints from.
	 * @param <E>   The type of the class to get the constraints for.
	 * @return A {@link SqlPredicate} describing the added constraints.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity> SqlPredicate<E> getConstraints(Class<E> clazz) {
		if (clazz == BaseEntity.class) {
			return (SqlPredicate<E>) selectConstraints.getOrDefault(BaseEntity.class, x -> true);
		}
		var existingPredicate = (SqlPredicate<E>) selectConstraints.getOrDefault(clazz, x -> true);
		Class<E> superClass = (Class<E>) clazz.getSuperclass();
		return existingPredicate.and(getConstraints(superClass));
	}

	/**
	 * Adds a query constraint to a query made with a certain entity.
	 * This means that any query made with this entity will include this {@code predicate}.
	 *
	 * @param clazz     The entity to add the constraint to.
	 * @param predicate The constraint.
	 * @param <E>       The type of the entity.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity> void addConstraint(Class<E> clazz, SqlPredicate<E> predicate) {
		if (selectConstraints.containsKey(clazz)) {
			var existingPredicate = (SqlPredicate<E>) selectConstraints.get(clazz);
			selectConstraints.replace(clazz, existingPredicate.and(predicate));
			return;
		}
		selectConstraints.put(clazz, predicate);
	}
}
