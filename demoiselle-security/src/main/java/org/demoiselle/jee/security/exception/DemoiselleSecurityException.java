/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import java.util.HashMap;

import org.demoiselle.jee.ws.jaxrs.exception.DemoiselleRESTException;

public class DemoiselleSecurityException extends DemoiselleRESTException {

    private static final long serialVersionUID = 519965615171844237L;

    private HashMap<String, String> messages = new HashMap<String, String>();

    private int statusCode;

    public DemoiselleSecurityException(String string) {
        super(string);
        this.statusCode = 401;
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
}
