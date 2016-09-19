package org.demoiselle.util;

import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * Utility class to peform useful operations on CDI discovered beans.
 *
 * @author SERPRO
 */
public final class CDIUtils {

	private static final Annotation[] annotationArrayType = new Annotation[0];

	/**
	 * Returns <code>true</code> if one annotation of the provided type is present
	 * on a list of annotations.
	 *
	 * @param annotationType Annotation type being looked for.
	 * @param allAnnotations List of all annotations where to look for.
	 * @return <code>true</code> if the annotation is present on the list
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean hasAnnotation(Class<? extends Annotation> annotationType, Annotation... allAnnotations) {
		for (Annotation currentAnnotation : allAnnotations) {
			if (currentAnnotation.annotationType().isAssignableFrom(annotationType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param annotationType Type of the annotation being checked
	 * @param allAnnotations List of annotations to check for the specific one
	 * @see #hasAnnotation(Class, Annotation...)
	 * @return <code>true</code> if the annotation is present on the list
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean hasAnnotation(Class<? extends Annotation> annotationType,
			Collection<Annotation> allAnnotations) {
		return hasAnnotation(annotationType, allAnnotations.toArray(annotationArrayType));
	}

	/**
	 * Returns <code>true</code> if a base class is annotated with the provided annotation.
	 *
	 * @param annotationType Annotation type to look for
	 * @param baseType Class to check for the informed annotation
	 * @see #hasAnnotation(Class, Annotation...)
	 * @return <code>true</code> if the annotation is present on the list
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean hasAnnotation(Class<? extends Annotation> annotationType, Class<?> baseType) {
		return hasAnnotation(annotationType, baseType.getAnnotations());
	}

	/**
	 * Returns the annotation instance that matches the annotation type provided,
	 * or <code>null</code> if no annotation of that type is present.
	 *
	 * @param annotationType Annotation type being looked for.
	 * @param allAnnotations List of all annotations where to look for.
	 * @param <T> Type of the specific annotation returned
	 * @return The annotation instance found, or <code>null</code> if there is no such annotation present.
	 */
	@SuppressWarnings({ "WeakerAccess", "unchecked" })
	public static <T extends Annotation> T getAnnotation(Class<T> annotationType, Annotation... allAnnotations) {
		for (Annotation currentAnnotation : allAnnotations) {
			if (currentAnnotation.annotationType().isAssignableFrom(annotationType)) {
				return (T) currentAnnotation;
			}
		}

		return null;
	}

	/**
	 * @param annotationType Annotation type being looked for.
	 * @param allAnnotations List of all annotations where to look for.
	 * @param <T> Type of the specific annotation returned
	 * @see #getAnnotation(Class, Annotation...)
	 * @return The annotation instance found, or <code>null</code> if there is no such annotation present.
	 */
	@SuppressWarnings({ "WeakerAccess" })
	public static <T extends Annotation> T getAnnotation(Class<T> annotationType,
			Collection<Annotation> allAnnotations) {
		return getAnnotation(annotationType, allAnnotations.toArray(annotationArrayType));
	}

	/**
	 * Returns <code>true</code> if one qualifier of the provided type is present
	 * on an injection point.
	 *
	 * @param qualifierAnnotationType Annotation type being looked for.
	 * @param ip                      Injection point of a bean type.
	 * @return <code>true</code> if the annotation is present on the list
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean hasQualifier(Class<? extends Annotation> qualifierAnnotationType, InjectionPoint ip) {
		return hasAnnotation(qualifierAnnotationType, ip.getQualifiers());
	}

	/**
	 * Returns the annotation instance that matches the annotation type provided,
	 * or <code>null</code> if no annotation of that type is present.
	 *
	 * @param qualifierAnnotationType Annotation type being looked for.
	 * @param ip                      Injection point of a bean type.
	 * @param <T> Type of the specific annotation returned
	 * @return The annotation instance found, or <code>null</code> if there is no such annotation present.
	 */
	@SuppressWarnings("WeakerAccess")
	public static <T extends Annotation> T getQualifier(Class<T> qualifierAnnotationType, InjectionPoint ip) {
		return getAnnotation(qualifierAnnotationType, ip.getQualifiers());
	}

}
