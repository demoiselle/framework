package org.demoiselle.jee.core.lifecycle.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.demoiselle.jee.core.annotation.Priority;

/**
 * Identifies a method eligible to be executed automatically during <b>application finalization</b>.
 * <p>
 * Take a look at the following usage sample:
 * </p>
 * 
 * <pre>
 * public class MyClass {
 * 
 *  &#064;Shutdown
 *  &#064;Priority(Priority.MIN_PRIORITY)
 *  public void finalize() {
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
public @interface Shutdown {

}
