package de.java2db.services;

import de.java2db.entities.BaseCodeAndDescriptionEntity;

import java.util.List;

/**
 * @author Collin Alpert
 */
public class BaseCodeAndDescriptionService<T extends BaseCodeAndDescriptionEntity> extends BaseService<T> {

	protected BaseCodeAndDescriptionService(Class<T> clazz) {
		super(clazz);
	}

	public T getByCode(String code) {
		return getSingle(x -> x.getCode() == code);
	}

	public List<T> getByDescription(String description) {
		return getMultiple(x -> x.getDescription() == description);
	}
}
