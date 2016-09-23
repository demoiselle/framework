/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import javax.enterprise.inject.spi.CDI;
import org.demoiselle.jee.core.annotation.literal.NameQualifier;
import org.demoiselle.jee.core.util.ResourceBundle;

/**
 * <p>
 * Thrown when the user's credentials are invalid.
 * </p>
 *
 * @author SERPRO
 */
public class InvalidCredentialsException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
        super(CDI.current().select(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle")).get().getString("invalid-credentials"));
    }

    /**
     * <p>
     * Constructs an <code>InvalidCredentialsException</code> with a message.
     * </p>
     *
     * @param message exception message.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * <p>
     * Constructor with message and cause.
     * </p>
     *
     * @param message exception message.
     * @param cause exception cause.
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
