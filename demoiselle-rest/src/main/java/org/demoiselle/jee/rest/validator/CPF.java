/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import org.demoiselle.jee.rest.validator.impl.CPFValidator;

/**
 *
 * @author 70744416353
 */
@Documented
@Constraint(validatedBy = {CPFValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@ReportAsSingleViolation
public @interface CPF {

    /**
     * Mensagem retornada quando a validação falhar.
     */
    String message() default "{cpf.error}";

    /**
     * Indica se o valor do campo é formatado ou não.
     */
    boolean formatted() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
