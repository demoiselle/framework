/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines the message key template for a method in a {@link MessageBundle} interface.
 * The value should be a key enclosed in curly braces, e.g. {@code "{my-message-key}"}.
 * The key is resolved against a ResourceBundle properties file that matches the
 * fully qualified name of the declaring interface.
 *
 * @author SERPRO
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface MessageTemplate {

    /**
     * The message key template, e.g. {@code "{my-key}"}.
     *
     * @return the message template string
     */
    String value();
}
