package org.demoiselle.jee.configuration.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Annotation used to suppress logger of a Object or Field
 * 
 * @author SERPRO
 *
 */
@Target({TYPE, FIELD})
@Retention(RUNTIME)
public @interface SuppressConfigurationLogger {

}
