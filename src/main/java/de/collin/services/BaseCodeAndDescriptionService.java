package de.collin.services;

import de.collin.entities.BaseCodeAndDescriptionEntity;

import java.util.List;

/**
 * @author Collin Alpert
 */
public abstract class BaseCodeAndDescriptionService<T extends BaseCodeAndDescriptionEntity> extends BaseService<T> {

	public BaseCodeAndDescriptionService(Class<T> clazz) {
		super(clazz);
	}

	public T getByCode(String code) {
		return getSingle(x -> x.getCode() == code);
	}

	public List<T> getByDescription(String description) {
		return getMultiple(x -> x.getDescription() == description);
	}
}
