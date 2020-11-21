package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.*;

/**
 * Sets the name of a column in a table for a POJO field.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnName {
	String value();
}
