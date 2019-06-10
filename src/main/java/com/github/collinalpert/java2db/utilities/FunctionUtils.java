package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.util.function.Consumer;

/**
 * A set of utility functions.
 *
 * @author Collin Alpert
 */
public class FunctionUtils {

	public static <T> Consumer<? super T> empty() {
		return c -> {
		};
	}

	public static <T> SqlPredicate<T> alwaysTrue() {
		return x -> true;
	}

	public static <T> SqlPredicate<T> alwaysFalse() {
		return x -> false;
	}
}
