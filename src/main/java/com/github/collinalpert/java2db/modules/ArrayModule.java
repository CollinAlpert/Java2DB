package com.github.collinalpert.java2db.modules;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * A helper module to work with arrays and especially adding elements to them.
 *
 * @param <T> The type of the array.
 * @author Collin Alpert
 */
public class ArrayModule<T> {

	/**
	 * The internal array which will be used for modifications.
	 */
	private T[] data;

	/**
	 * The size an array will be extended by when its size limit is reached.
	 * This is the main difference to an {@link java.util.ArrayList}, which will only extend the internal array by 1 every time an element is added.
	 * The hope is that an increase by multiple array slots will also increase the performance.
	 */
	private int chunkSize;

	private int counter;

	@SuppressWarnings("unchecked")
	public ArrayModule(Class<T> clazz, int chunkSize) {
		this.data = (T[]) Array.newInstance(clazz, chunkSize);
		this.chunkSize = chunkSize;
	}

	/**
	 * Gets an array with all the elements that were added to it.
	 * Should there be excess slots from when the array was extended to fit more elements, they will be trimmed away.
	 *
	 * @return An array containing the added elements.
	 */
	public T[] getArray() {
		return trimArray(this.data);
	}

	/**
	 * Adds an element to the array.
	 * When the max size of the array is reached, it is extended by the specified {@code chunkSize} in the constructor.
	 *
	 * @param element The element to add to the array.
	 */
	public void addElement(T element) {
		if (this.data.length - 1 == this.counter) {
			this.data = Arrays.copyOf(this.data, this.data.length + this.chunkSize);
		}

		this.data[counter++] = element;
	}

	/**
	 * Trims trailing empty slots from an array.
	 *
	 * @param array The array to trim.
	 * @return A new array without possible empty trailing slots.
	 */
	private T[] trimArray(T[] array) {
		var lastElement = 0;
		for (var i = array.length - 1; i >= 0; i--) {
			if (array[i] != null) {
				lastElement = i;
				break;
			}
		}

		return Arrays.copyOf(array, lastElement + 1);
	}
}
