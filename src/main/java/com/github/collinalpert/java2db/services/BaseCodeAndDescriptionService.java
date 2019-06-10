package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.entities.BaseCodeAndDescriptionEntity;
import com.github.collinalpert.java2db.queries.EntityQuery;

import java.util.Optional;

/**
 * Describes a service class for an entity which contains an id, a code and a description.
 *
 * @author Collin Alpert
 */
public class BaseCodeAndDescriptionService<T extends BaseCodeAndDescriptionEntity> extends BaseService<T> {

	/**
	 * Retrieves an entry from a table based on its unique code.
	 *
	 * @param code The code to get the entity by.
	 * @return An entity matching this code. It is assumed that a code, just like the id, is unique in a table.
	 */
	public Optional<T> getByCode(String code) {
		return getFirst(x -> x.getCode() == code);
	}

	/**
	 * Retrieves entries from a table based on their description.
	 * This is an uncommon method to use but exists for completeness sakes.
	 *
	 * @param description The description to get the results by.
	 * @return A list of entities matching a certain description.
	 */
	public EntityQuery<T> getByDescription(String description) {
		return getMultiple(x -> x.getDescription() == description);
	}
}
