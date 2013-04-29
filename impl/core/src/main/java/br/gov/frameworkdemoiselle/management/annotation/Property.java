package br.gov.frameworkdemoiselle.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * <p>Indicates that a field must be exposed as a property to management clients.</p>
 * <p>The property will be writable if there's a public setter method
 * declared for the field and readable if there's a getter method.</p>
 * <p>It's a runtime error to annotate a field with no getter and no setter method.</p>
 * <p>It's also a runtime error to declare a field as a property and one or both of it's getter and setter
 * methods as an operation using the {@link Operation} annotation.</p> 
 * 
 * @author SERPRO
 *
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
	
	/**
	 * @return The description of this property exposed to management clients.
	 */
	@Nonbinding
	String description() default "";

}
