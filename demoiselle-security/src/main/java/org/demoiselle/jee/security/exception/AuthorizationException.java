/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

/**
 * <p>
 * Thrown when a fail on trying to access some resource and/or execute an
 * operation without the proper authorization.
 * </p>
 *
 * @author SERPRO
 */
public class AuthorizationException extends SecurityException {

    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor with message.
     * </p>
     *
     * @param message exception message
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * <p>
     * Constructor with the cause.
     * </p>
     *
     * @param cause exception cause
     */
    public AuthorizationException(Throwable cause) {
        super(cause);
    }
}
