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
 * Identifies a <b>persistence controller</b> class. A persistence controller is
 * a layer which provides simplified access to data stored in persistent storage
 * of some kind, such as an entity-relational database.
 * <p>
 * A <i>Persitence Controller</i> is:
 * <ul>
 * <li>defined when annotated with {@code @PersistenceController}</li>
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
public @interface PersistenceController {
}
