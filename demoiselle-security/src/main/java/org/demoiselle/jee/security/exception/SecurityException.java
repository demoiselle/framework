/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * <code>SecurityException</code> is the superclass of those exceptions that can
 * be thrown due to any security related issue.
 *
 * @author SERPRO
 */
public class SecurityException extends DemoiselleException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>SecurityException</code> with the specified detail
     * message.
     *
     * @param message the detail message.
     */
    SecurityException(String message) {
        super(message);
    }

    /**
     * Constructor with the cause.
     *
     * @param cause exception cause
     */
    SecurityException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message exception message
     * @param cause exception cause
     */
    SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
