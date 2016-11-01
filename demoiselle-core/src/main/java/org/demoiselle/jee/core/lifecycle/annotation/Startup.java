package org.demoiselle.jee.core.lifecycle.annotation;

import java.lang.annotation.Target;

import org.demoiselle.jee.core.annotation.Priority;

import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a method eligible to be executed automatically during <b>application initialization</b>.
 * <p>
 * Take a look at the following usage sample:
 * </p>
 * 
 * <pre>
 * public class MyClass {
 * 
 *  &#064;Startup
 *  &#064;Priority(Priority.MAX_PRIORITY)
 *  public void init() {
 *    ...
 *  }
 * }
 * </pre>
 * 
 * See {@link Priority}
 * 
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Startup {

}
