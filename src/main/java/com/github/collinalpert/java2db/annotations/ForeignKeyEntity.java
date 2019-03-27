package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the correspondent object to a foreign key.
 * This property does not have to exist on the database.
 * Its parameter is the name of the foreign key column.
 *
 * This annotation can be used on two types of objects:
 * 1) Entities which represent real objects of foreign key tables. In this case the entity has to extend
 * {@link com.github.collinalpert.java2db.entities.BaseEntity}. Because this entity represents a table it should extend {@code BaseEntity} anyway.
 *
 * 2) Foreign keys to a table with static values that can be represented by an enum because they don't change.
 * In that case the enum must extend {@link com.github.collinalpert.java2db.contracts.IdentifiableEnum} and map the ids from the table.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKeyEntity {
	String value();
}
