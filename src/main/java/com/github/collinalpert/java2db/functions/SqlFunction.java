package com.github.collinalpert.java2db.functions;

import java.util.function.Function;

/**
 * @author Collin Alpert
 */
@FunctionalInterface
public interface SqlFunction<T, R> extends Function<T, R>, SerializedFunctionalInterface {
}
