package org.demoiselle.jee.persistence.crud.manager;

import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.*;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Inherited
@InterceptorBinding
@Target({TYPE})
@Retention(RUNTIME)
public @interface CrudRest {
	
	@Nonbinding
	Class<?> model() default Class.class;

}
