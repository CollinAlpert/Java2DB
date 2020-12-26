package com.github.collinalpert.java2db.annotations;

import com.github.collinalpert.java2db.entities.BaseEntity;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that only a specific column of a table is supposed to be joined when executing the query.
 * It must be used in conjunction with the {@link ForeignKeyEntity} attribute.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKeyPath {

	/**
	 * @return The name of the column on the table which will be joined.
	 */
	String value();

	/**
	 * @return The class which represents the table which will be joined.
	 */
	Class<? extends BaseEntity> foreignKeyClass();
}
