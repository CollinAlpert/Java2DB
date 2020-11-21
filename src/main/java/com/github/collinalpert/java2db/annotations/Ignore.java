package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.*;

/**
 * Marks a field as ignored, meaning it does not exist on the database or should not be filled with values.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
