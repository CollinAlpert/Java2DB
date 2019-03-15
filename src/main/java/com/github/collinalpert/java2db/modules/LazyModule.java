package com.github.collinalpert.java2db.modules;

import java.util.function.Supplier;

/**
 * A helper module to support lazy loading of objects.
 *
 * @param <T> The type of object to instantiate lazily.
 * @author Collin Alpert
 */
public class LazyModule<T> {

	private final Supplier<T> valueFactory;
	private T value;

	public LazyModule(Supplier<T> valueFactory) {
		this.valueFactory = valueFactory;
	}

	public T getValue() {
		if (value != null) {
			return value;
		}

		return value = valueFactory.get();
	}
}
