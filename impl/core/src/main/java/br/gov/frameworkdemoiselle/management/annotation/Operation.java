package br.gov.frameworkdemoiselle.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.management.MBeanException;

/**
 * <p>Indicates that a method is an operation, meaning you can manage some aspect of the application by calling it.</p>
 * <p>This annotation can't be used together with {@link Property}, doing so will throw a {@link MBeanException}.</p>  
 * 
 * @author SERPRO
 *
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation {
	
	/**
	 * Description that will be used to publish the operation to clients.
	 * Defaults to an empty description.
	 */
	@Nonbinding
	String description() default "";
	
	/**
	 * Type of operation. Defaults to {@link OperationType#UNKNOWN}.
	 */
	@Nonbinding
	OperationType type() default OperationType.UNKNOWN;

}
