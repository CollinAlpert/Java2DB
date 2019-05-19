package com.github.collinalpert.java2db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation tells Java2DB to use the database-default for a column if the corresponding Java field marked with this annotation is {@code null}.
 *
 * @author Collin Alpert
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultIfNull {

	/**
	 * Configures, if the database-default should be used in a create statement when the field is {@code null}.
	 * Per default, this is enabled.
	 *
	 * @return {@code True}, if the database-default should be used in create statements, {@code false} if {@code null} should be written into the database when the field is also {@code null}.
	 */
	boolean onCreate() default true;

	/**
	 * Configures, if the database-default should be used in an update statement when the field is {@code null}.
	 * Per default, this is <em>not</em> enabled.
	 *
	 * @return {@code True}, if the database-default should be used in update statements, {@code false} if {@code null} should be written into the database when the field is also {@code null}.
	 */
	boolean onUpdate() default false;
}
