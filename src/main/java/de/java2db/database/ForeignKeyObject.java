package de.java2db.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the correspondent object to a foreign key.
 * The parameter value has to match the corresponding {@link ForeignKey} parameter value.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKeyObject {
	int value();
}
