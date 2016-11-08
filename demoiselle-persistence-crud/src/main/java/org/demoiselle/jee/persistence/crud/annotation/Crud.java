/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 *
 * @author 70744416353
 */
@Inherited
@InterceptorBinding
@Target({TYPE})
@Retention(RUNTIME)
public @interface Crud {

    @Nonbinding
    boolean security() default true;

    @Nonbinding
    boolean cors() default true;

}
