/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>Used in fields of classes annotated with
 * to indicate that the system should ignore this field when population the new configuration
 * instance with values extracted from the source file.</p>
 *
 * @author SERPRO
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ConfigurationIgnore {
}
