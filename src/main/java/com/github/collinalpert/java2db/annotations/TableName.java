package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the database table name for an entity.
 * If this annotation is not specified, the class name as lower case will be used.
 *
 * @author Collin Alpert
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
	String value();
}
