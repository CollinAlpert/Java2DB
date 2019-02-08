package com.github.collinalpert.java2db.entities;

/**
 * Describes an entity that has an id and an isDeleted flag.
 *
 * @author Collin Alpert
 */
public class BaseDeletableEntity extends BaseEntity {

	private boolean isDeleted;

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	@Override
	public String toString() {
		return "IsDeleted: " + isDeleted + ", " + super.toString();
	}
}
