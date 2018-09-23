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

	/**
	 * This setter only exists for frameworks like Spring, where a form needs to set this id.
	 * It is <b>greatly</b> discouraged from using this setter directly.
	 *
	 * @param id The it of the entity.
	 */
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Id: " + id;
	}
}
