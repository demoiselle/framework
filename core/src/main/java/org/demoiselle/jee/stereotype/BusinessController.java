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
 * Identifies a <b>business controller</b> class. Business controller objects typically implement the controller design
 * pattern, i.e., they contain no data elements but methods that orchestrate interaction among business entities.
 * <p>
 * A <i>Business Controller</i> is:
 * <ul>
 * <li>defined when annotated with {@code @BusinessController}</li>
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
public @interface BusinessController {
}
