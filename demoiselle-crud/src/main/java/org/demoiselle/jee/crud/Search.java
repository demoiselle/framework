/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in methods that want to change the 
 * default paging behavior and determine the fields that can be used by the 
 * request and in the return of the result.
 * 
 * Ex.
 * 
 * <pre>
 * &#64;GET
 * &#64;Search(fields={"field1", "field2"}, withPagination = true, quantityPerPage = 2)
 * public Result myNewMethod(){
 *    ...
 * }
 * </pre>
 * 
 * The method above will filter just 'field1' and 'field2' and the default pagination page will be 2 
 * register per page.
 * 
 * @author SERPRO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface Search {
    String[] fields();
    boolean withPagination() default true;
    int quantityPerPage() default 20;
}
