/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

import java.util.HashMap;

import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.exception.DemoiselleException;

public class DemoiselleRestException extends DemoiselleException {

    private static final long serialVersionUID = 519_965_615_171_844_237L;

    private final HashMap<String, String> messages = new HashMap<>();

    private int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();

    public DemoiselleRestException(String string) {
        super(string);
        this.statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    public DemoiselleRestException(String string, int statusCode) {
        super(string);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void addMessage(String field, String msg) {
        messages.put(field, msg);
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public DemoiselleRestException() {
    }

    public DemoiselleRestException(Throwable cause) {
        super(cause);
    }

    public DemoiselleRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoiselleRestException(String message, int statusCode, Throwable cause) {
        super(message, cause);
    }

}
