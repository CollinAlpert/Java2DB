package com.github.collinalpert.java2db.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * A helper module which provides functionality for getting information from annotations.
 *
 * @author Collin Alpert
 */
public class AnnotationModule {

	private static final AnnotationModule instance;

	static {
		instance = new AnnotationModule();
	}

	private AnnotationModule() {
	}

	public static AnnotationModule getInstance() {
		return instance;
	}

	/**
	 * Checks if a field was annotated with a specific annotation.
	 *
	 * @param field           The field to check.
	 * @param annotationClass The annotation to check for.
	 * @param <A>             The type of the annotation.
	 * @return {@code True}, if the field was annotated with this annotation, {@code false} otherwise.
	 */
	public <A extends Annotation> boolean hasAnnotation(Field field, Class<A> annotationClass) {
		return field.getAnnotation(annotationClass) != null;
	}

	/**
	 * Checks if a field was annotated with a specific annotation and performs an additional condition if so.
	 *
	 * @param field               The field to check.
	 * @param annotationClass     The annotation to check for.
	 * @param additionalCondition An additional condition to perform on the annotation.
	 *                            This condition is only executed if the field was annotated with the specified annotation.
	 * @param <A>                 The type of the annotation.
	 * @return {@code True}, if the field was annotated with this annotation and the additional condition evaluates to {@code true}, {@code false} otherwise.
	 */
	public <A extends Annotation> boolean hasAnnotation(Field field, Class<A> annotationClass, Function<A, Boolean> additionalCondition) {
		A annotation;
		if ((annotation = field.getAnnotation(annotationClass)) == null) {
			return false;
		}

		return additionalCondition.apply(annotation);
	}

	/**
	 * Gets information to an annotation for a field.
	 *
	 * @param field           The field to get annotation information from.
	 * @param annotationClass The annotation to check for.
	 * @param <A>             The type of the annotation.
	 * @return Information concerning a specific annotation of a field.
	 */
	public <A extends Annotation> AnnotationInfo<A> getAnnotationInfo(Field field, Class<A> annotationClass) {
		return getAnnotationInfo(field, annotationClass, x -> true);
	}

	/**
	 * Gets information to an annotation for a field and performs an additional condition if the field has been annotated.
	 *
	 * @param field               The field to get annotation information from.
	 * @param annotationClass     The annotation to check for.
	 * @param additionalCondition An additional condition to perform on the annotation.
	 *                            This condition is only executed if the field was annotated with the specified annotation.
	 * @param <A>                 The type of the annotation.
	 * @return Information concerning a specific annotation of a field.
	 */
	public <A extends Annotation> AnnotationInfo<A> getAnnotationInfo(Field field, Class<A> annotationClass, Function<A, Boolean> additionalCondition) {
		A annotation;
		if ((annotation = field.getAnnotation(annotationClass)) == null) {
			return new AnnotationInfo<>();
		}

		return new AnnotationInfo<>(additionalCondition.apply(annotation), annotation);
	}

	/**
	 * A helper class for determining information of an annotation above a field.
	 *
	 * @param <A> The type of the annotation.
	 */
	public static final class AnnotationInfo<A extends Annotation> {

		/**
		 * Specifies that the annotation exists in the current context. If this is {@code false}, the "annotation" field will always be {@code null}.
		 */
		private final boolean hasAnnotation;

		/**
		 * The annotation instance of a field, if the field was actually annotated with it. If this field has a value, the "hasAnnotation" field will always be {@code true}.
		 */
		private final A annotation;

		public AnnotationInfo() {
			this(false, null);
		}

		public AnnotationInfo(boolean hasAnnotation, A annotation) {
			this.hasAnnotation = hasAnnotation;
			this.annotation = annotation;
		}

		public boolean hasAnnotation() {
			return hasAnnotation;
		}

		public A getAnnotation() {
			return annotation;
		}
	}
}
