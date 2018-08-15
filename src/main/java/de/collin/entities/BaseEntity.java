package de.collin.entities;


/**
 * @author Collin Alpert
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
