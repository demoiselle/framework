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
 * Limits the rate of requests for the annotated method or class.
 * When the number of requests exceeds the configured limit within the
 * time window, subsequent requests are rejected with HTTP 429 (Too Many Requests).
 * </p>
 *
 * @see <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 * @author SERPRO
 */
@Inherited
@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface RateLimit {

    @Nonbinding
    int requests() default 100;

    @Nonbinding
    int window() default 60; // segundos
}
