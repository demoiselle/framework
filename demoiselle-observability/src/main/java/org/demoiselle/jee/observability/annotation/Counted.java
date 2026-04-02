package org.demoiselle.jee.observability.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

/**
 * Interceptor binding que incrementa um contador MicroProfile Metrics
 * para cada invocação do método anotado.
 */
@Inherited
@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface Counted {
    /** Nome do contador no formato "demoiselle.<modulo>.<operacao>" */
    @Nonbinding String value() default "";
}
