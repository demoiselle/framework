/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * <p>
 * String based non-binding qualifier.
 * </p>
 *
 * <p>
 * This annotation is used to qualify beans using an user defined String. 
 * methods can then read this string and use it to customize the bean creation process.
 * </p>
 *
 * <p>
 * The  attribute is non-binding (contrary to meaning multiple classes
 * qualified with this annotation, even with different values, will be considered the same candidate for
 * injection points. To avoid ambiguous resolutions and select which candidate to choose usually you'll need a
 * producer method to read the string and select the best fitted candidate.
 * </p>
 *
 * <p>
 * The framework classes qualified with this annotation already have such producers and the accepted values for
 * this annotation will be detailed in their respective documentations.
 * </p>
 *
 *
 * @author SERPRO
 *
 */
@Qualifier
@Inherited
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER })
public @interface ConfigurationName {

	/**
	 * <p>
	 * Specifies a name to access a custom configuration that will change how the annotated bean works.
	 * </p>
	 * <p>
	 * This attribute is nonbinding so you can use the  annotation to create
	 * methods or fields and have only one producer that works with all injection points no matter the value of this attribute.
	 * </p>
	 * @return Name of custom settings to personalize how the annotated bean works.
	 */
	@Nonbinding
	String value() default "";

}
