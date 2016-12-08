/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import javax.interceptor.InterceptorBinding;

import org.demoiselle.jee.configuration.ConfigType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 * Identifies a <b>configuration class</b>, that is, a structure reserved to store configuration values retrieved from a
 * given resource file or system variables.
 * 
 * <p>
 * This class is gonna have a single instance throughout the application, as stated by the <b>singleton</b> design
 * pattern approach.
 * <p>
 * 
 * A <i>Configuration</i> is:
 * <ul>
 * <li>defined when annotated with {@code @Configuration}</li>
 * <li>automatically injected whenever {@code @Inject} is used</li>
 * </ul>
 * 
 */

@ApplicationScoped
@Named
@InterceptorBinding
@Stereotype
@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {

	
	String DEFAULT_PREFIX = "demoiselle";

	/**
	 * Define the default resource.
	 */
	String DEFAULT_RESOURCE = "demoiselle";

	/**
	 * Defines the resource type to be used: a properties file, an XML file or system variables.
	 * <p>
	 * If not specified, a properties resource file is to be considered.
	 * </p>
	 * 
	 * @return {@link ConfigType}
	 */
	@Nonbinding ConfigType type() default ConfigType.PROPERTIES;

	/**
	 * Defines an optional prefix to be used on every parameter key.
	 * <p>
	 * For instance, if prefix is set to <b>"demoiselle.pagination"</b> and an attribute named
	 * <b>defaultPageSize</b> is found in the class, the corresponding key
	 * <b>demoiselle.pagination.defaultPageSize</b> is expected to be read in the resource file.
	 * </p>
	 * 
	 * @return String
	 */
	@Nonbinding String prefix() default DEFAULT_PREFIX;

	/**
	 * Defines the resource file name to be read by this configuration class. There is no need to specify file extension
	 * in the case of properties or XML resources.
	 * 
	 * <p>
	 * For instance, when resource is set to <b>"bookmark"</b> and the type set to properties, a corresponding
	 * file named <b>bookmark.properties</b> is considered.	 
	 * </p>
	 * 
	 * <p>
	 * If not specified, the default configuration file <b>demoiselle.properties</b> is rather considered.
	 * </p>
	 * 
	 * @return String
	 */
	@Nonbinding String resource() default DEFAULT_RESOURCE;

}
