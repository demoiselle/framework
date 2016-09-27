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
 * <p>
 * Identifies a <b>configuration class</b>, that is, a structure reserved to store configuration values retrieved from a
 * given resource file or system variables.
 * </p>
 * <p>
 * Configuration resources are application scoped, meaning only one instance can ever exist in
 * a running application. For that reason usually configuration fields are immutable, to avoid
 * changes made in one context affecting other contexts in a running application.
 * </p>
 * <p>A <i>Configuration</i> is:</p>
 * <ul>
 * <li>defined when annotated with {@code @Configuration}</li>
 * <li>automatically injected whenever {@code @Inject} is used</li>
 * </ul>
 *
 * @author SERPRO
 */
@ApplicationScoped
@Named
@InterceptorBinding
@Stereotype
@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {

	/**
	 * Define the default prefix.
	 */
	String DEFAULT_PREFIX = "demoiselle";

	/**
	 * Define the default resource.
	 */
	String DEFAULT_RESOURCE = "demoiselle";

	/**
	 * Defines the resource type to be used: a properties file, an XML file, or system variables.
	 * If not specified, a properties resource file is to be considered.
	 *
	 * @return ConfigType Type of configuration resource file to look for
	 */
	@Nonbinding ConfigType type() default ConfigType.PROPERTIES;

	/**
	 * Defines an optional prefix to be used on every parameter key.
	 * For instance, if prefix is set to <code>"demoiselle.pagination"</code> and an attribute named
	 * <code>defaultPageSize</code> is found in the class, the corresponding key
	 * <code>demoiselle.pagination.defaultPageSize</code> is expected to be read in the resource file.
	 *
	 * @return String prefix common to all attributes to be read by the configuration class
	 */
	@Nonbinding String prefix() default DEFAULT_PREFIX;

	/**
	 * Defines the resource file name to be read by this configuration class. There is no need to specify file extension
	 * in the case of properties or XML resources.
	 * For instance, when resource is set to <code>"bookmark"</code> and the type set to properties, a corresponding
	 * file named <code>bookmark.properties</code> is considered.
	 * If not specified, the default configuration file <code>demoiselle.properties</code> is used instead.
	 *
	 * @return String Name of the resource file to look for (minus file extension)
	 */
	@Nonbinding String resource() default DEFAULT_RESOURCE;

}
