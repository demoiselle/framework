/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.rest.exception.DemoiselleRestException;

/**
 *
 * @author SERPRO
 */
public class DemoiselleSecurityException extends DemoiselleRestException {

    private static final long serialVersionUID = 519_965_615_171_844_237L;

    public DemoiselleSecurityException(String string) {
        super(string);
        this.statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    public DemoiselleSecurityException(String string, int statusCode) {
        super(string);
        this.statusCode = statusCode;
    }

    public DemoiselleSecurityException(String string, int statusCode, Exception ex) {
        super(string);
        this.statusCode = statusCode;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

}
