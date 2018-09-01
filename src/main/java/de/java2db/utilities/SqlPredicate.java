package de.java2db.utilities;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Collin Alpert
 * <p>
 * A serialized predicate.
 * </p>
 */
@FunctionalInterface
public interface SqlPredicate<T> extends Predicate<T>, Serializable {

	boolean test(T t);

	default SqlPredicate<T> and(SqlPredicate<? super T> other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}
}
