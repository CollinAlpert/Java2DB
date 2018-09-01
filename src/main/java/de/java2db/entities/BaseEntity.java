package de.java2db.entities;


/**
 * @author Collin Alpert
 * <p>
 * Describes an entity that has an id. Every entity must inherit from this class.
 * </p>w
 */
public class BaseEntity {

	private int id;

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Id: " + id;
	}
}
