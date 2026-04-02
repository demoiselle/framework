/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

/**
 *  
 * @author SERPRO
 */
@Inherited
@InterceptorBinding
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface CacheControl {

    @Nonbinding
    String value() default "";

    @Nonbinding
    int maxAge() default -1;

    @Nonbinding
    int sMaxAge() default -1;

    @Nonbinding
    boolean noCache() default false;

    @Nonbinding
    boolean noStore() default false;

    @Nonbinding
    boolean mustRevalidate() default false;

    @Nonbinding
    boolean isPrivate() default false;
}
