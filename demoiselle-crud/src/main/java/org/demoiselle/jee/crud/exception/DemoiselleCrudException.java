/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.exception;

import org.demoiselle.jee.rest.exception.DemoiselleRestException;

/**
 *
 * Exception for all persistence errors in CRUD.
 *
 * @author SERPRO
 */
public class DemoiselleCrudException extends DemoiselleRestException {

    private static final long serialVersionUID = 1L;

    public DemoiselleCrudException() {
    }

    public DemoiselleCrudException(String message) {
        super(message);
    }

    public DemoiselleCrudException(Throwable cause) {
        super(cause);
    }

    public DemoiselleCrudException(String message, Throwable cause) {
        super(message, cause);
    }

}
