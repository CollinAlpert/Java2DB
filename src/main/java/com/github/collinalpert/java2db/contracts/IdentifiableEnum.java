package com.github.collinalpert.java2db.contracts;

/**
 * A contract for enums which represent foreign keys.
 * Every enum used to represent a foreign key must implement this interface.
 *
 * @author Collin Alpert
 */
public interface IdentifiableEnum {
	long getId();
}
