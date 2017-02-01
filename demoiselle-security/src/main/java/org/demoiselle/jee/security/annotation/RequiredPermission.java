/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * <p>
 * Indicates that a specific permission is required in order to invocate the
 * annotated method or class.
 * </p>
 * 
 * @see <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 * @author SERPRO
 */
@Inherited
@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface RequiredPermission {

    @Nonbinding
    String resource() default "";

    @Nonbinding
    String operation() default "";
}
