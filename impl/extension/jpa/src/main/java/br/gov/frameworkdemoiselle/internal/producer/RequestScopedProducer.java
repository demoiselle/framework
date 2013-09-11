package br.gov.frameworkdemoiselle.internal.producer;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Stereotype;

@RequestScoped
@Stereotype
@Inherited
@Retention(RUNTIME)
@Target({ TYPE })
public @interface RequestScopedProducer {

}
