/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import org.demoiselle.jee.rest.exception.DemoiselleRestException;

/**
 *
 * @author SERPRO
 */
public class DemoiselleSecurityException extends DemoiselleRestException {

    private static final long serialVersionUID = 519_965_615_171_844_237L;

    public DemoiselleSecurityException(String string) {
        super(string);
    }

    public DemoiselleSecurityException(String string, int statusCode) {
        super(string, statusCode);
    }

    public DemoiselleSecurityException() {
    }

    public DemoiselleSecurityException(Throwable cause) {
        super(cause);
    }

    public DemoiselleSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoiselleSecurityException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

}
