package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;

/**
 * A set of utilities for the callbacks in asynchronous methods.
 *
 * @author Collin Alpert
 */
public class CallbackUtils {

	public static <T> Consumer<? super T> empty() {
		return c -> {
		};
	}
}
