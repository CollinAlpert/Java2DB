package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.entities.BaseCodeAndDescriptionEntity;

import java.util.List;

/**
 * @author Collin Alpert
 * <p>
 * Describes a service class for an entity which contains an id, a code and a description.
 * </p>
 */
public class BaseCodeAndDescriptionService<T extends BaseCodeAndDescriptionEntity> extends BaseService<T> {

	public BaseCodeAndDescriptionService(Class<T> clazz) {
		super(clazz);
	}

	/**
	 * @param code The code to get the entity from.
	 * @return An entity matching this code. It is assumed that a code, just like the id, is unique in a table.
	 */
	public T getByCode(String code) {
		return getSingle(x -> x.getCode() == code);
	}

	/**
	 * @param description The description to get the results by.
	 * @return A list of entities matching a certain description.
	 */
	public List<T> getByDescription(String description) {
		return getMultiple(x -> x.getDescription() == description);
	}
}
