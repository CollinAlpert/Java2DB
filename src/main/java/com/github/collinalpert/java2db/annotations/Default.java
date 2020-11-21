package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.*;

/**
 * This annotation tells Java2DB to always use the database-default for a column on create or update. Not to be confused with the {@link DefaultIfNull} annotation.
 *
 * @author Collin Alpert
 * @see DefaultIfNull
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
}
