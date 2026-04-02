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

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

/**
 * <p>
 * Indicates that the annotated method or class requires the user to have
 * at least one of the specified roles (OR logic) in order to be invoked.
 * </p>
 *
 * @see <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 * @author SERPRO
 */
@Inherited
@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface RequiredAnyRole {

    @Nonbinding
    String[] value();
}
