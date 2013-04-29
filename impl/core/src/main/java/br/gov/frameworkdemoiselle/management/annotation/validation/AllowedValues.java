package br.gov.frameworkdemoiselle.management.annotation.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;

import br.gov.frameworkdemoiselle.management.internal.validators.AllowedValuesValidator;

@Documented
@Target({ FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AllowedValuesValidator.class)
/**
 * Validate a value against a list of allowed values.
 * 
 * @author serpro
 *
 */
public @interface AllowedValues {
	
	/**
	 * List of accepted values
	 */
	String[] allows();
	
	/**
	 * Type of allowed values. Defaults to {@link ValueType#STRING}.
	 */
	ValueType valueType() default ValueType.STRING;
	
	enum ValueType {
		STRING,INTEGER,DECIMAL;
	}
	
}
