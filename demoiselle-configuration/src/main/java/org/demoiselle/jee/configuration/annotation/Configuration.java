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

@ApplicationScoped
@Named
@InterceptorBinding
@Stereotype
@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {

	String DEFAULT_PREFIX = "demoiselle";

	String DEFAULT_RESOURCE = "demoiselle";

	@Nonbinding ConfigType type() default ConfigType.PROPERTIES;

	@Nonbinding String prefix() default DEFAULT_PREFIX;

	@Nonbinding String resource() default DEFAULT_RESOURCE;

}
