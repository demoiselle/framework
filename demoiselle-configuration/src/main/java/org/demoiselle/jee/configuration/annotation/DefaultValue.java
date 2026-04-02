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
 * Defines a default value for a configuration field. When the corresponding key
 * is not present in the configuration source, the value specified in this annotation
 * is converted to the field type and assigned.
 *
 * <p>Supported field types: String, primitives, primitive wrappers, and enums.</p>
 *
 * @author SERPRO
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DefaultValue {

    /**
     * The default value as a String, to be converted to the field's type.
     *
     * @return the default value
     */
    String value();
}
