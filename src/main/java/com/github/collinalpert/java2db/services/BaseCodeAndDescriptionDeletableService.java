package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.entities.BaseCodeAndDescriptionDeletableEntity;
import com.github.collinalpert.java2db.queries.EntityQuery;

import java.util.Optional;

/**
 * Describes a service class for an entity which contains an id, an isDeleted flag, a code and a description.
 * Note that when deleting entities which have an isDeleted flag with this service,
 * they will not actually be deleted from the database, but the flag will be set to {@code true}.
 *
 * @author Collin Alpert
 */
public class BaseCodeAndDescriptionDeletableService<T extends BaseCodeAndDescriptionDeletableEntity> extends BaseDeletableService<T> {

	/**
	 * Retrieves an entry from a table based on its unique code.
	 *
	 * @param code The code to get the entity from.
	 * @return An entity matching this code. It is assumed that a code, just like the id, is unique in a table.
	 */
	public Optional<T> getByCode(String code) {
		return getFirst(x -> x.getCode() == code);
	}

	/**
	 * Retrieves entries from a table based on their description. This is an uncommon method but exists for completeness sakes.
	 *
	 * @param description The description to get the results by.
	 * @return A list of entities matching a certain description.
	 */
	public EntityQuery<T> getByDescription(String description) {
		return getMultiple(x -> x.getDescription() == description);
	}
}
