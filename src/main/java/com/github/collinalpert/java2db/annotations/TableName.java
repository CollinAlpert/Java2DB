package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.*;

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
