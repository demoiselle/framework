/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.stereotype;

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a <b>facade</b> class. A facade is an object that provides a
 * simplified interface to a larger body of code, such as a class library.
 * <p>
 * A <i>Facade</i> is:
 * <ul>
 * <li>defined when annotated with {@code @FacadeController}</li>
 * <li>automatically injected whenever {@code @Inject} is used</li>
 * </ul>
 *
 * @author SERPRO
 * @see Controller
 */
@Controller
@Stereotype
@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface RestController {
}
