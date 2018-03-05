/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
public @interface DemoiselleCrud {
    Class<?> value() default Object.class;
    boolean enableSort() default true;
    boolean enablePagination() default true;
    int pageSize() default 20;
    boolean enableSearch() default true;
    String[] searchFields() default {"*"};
    boolean enableFilterFields() default true;
    String[] filterFields() default {};
}
