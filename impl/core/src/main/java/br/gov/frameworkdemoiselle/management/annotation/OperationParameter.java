package br.gov.frameworkdemoiselle.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * <p>Optional annotation to write additional detail about an operation's parameter.</p>
 * <p>This annotation is ignored for non-operation methods.</p>  
 * 
 * @author SERPRO
 *
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationParameter {
	
	/**
	 * Name that will be used to publish this operation's parameter to clients.
	 */
	@Nonbinding
	String name();
	
	/**
	 * Optional description that will be used to publish this operation's parameter to clients.
	 * Defaults to an empty description.
	 */
	@Nonbinding
	String description() default "";
	

}
