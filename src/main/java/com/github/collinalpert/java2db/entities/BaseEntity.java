package com.github.collinalpert.java2db.entities;


/**
 * @author Collin Alpert
 * <p>
 * Describes an entity that has an id. Every entity must inherit from this class.
 * </p>w
 */
public class BaseEntity {

	private long id;

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Id: " + id;
	}
}
