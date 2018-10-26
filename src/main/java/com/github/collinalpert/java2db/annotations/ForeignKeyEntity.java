package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the correspondent object to a foreign key.
 * This property does not have to exist on the database.
 * Its parameter is the name of the field of the foreign key.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKeyEntity {
	String value();
}
