package de.java2db.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Collin Alpert
 * <p>
 * Specifies the database table name for an entity. If this annotation is not specified, the class name as lower case will be used.
 * </p>
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
	String value();
}
