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

    private HashMap<String, String> messages = new HashMap<String, String>();

    private int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();

    //TODO incluir construtor com codigo de erro
    public DemoiselleRestException() {
    }

    public DemoiselleRestException(String string) {
        super(string);
    }

    public DemoiselleRestException(Throwable cause) {
        super(cause);
    }

    public DemoiselleRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void addMessage(String field, String msg) {
        this.statusCode = 422;
        messages.put(field, msg);
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }
}
