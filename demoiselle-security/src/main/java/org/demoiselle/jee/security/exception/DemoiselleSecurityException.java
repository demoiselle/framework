/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import java.util.HashMap;

import org.demoiselle.jee.rest.exception.DemoiselleRESTException;

/**
 *
 * @author 70744416353
 */
public class DemoiselleSecurityException extends DemoiselleRESTException {

    private static final long serialVersionUID = 519_965_615_171_844_237L;

    private final HashMap<String, String> messages = new HashMap<>();

    private final int statusCode;

    public DemoiselleSecurityException(String string) {
        super(string);
        this.statusCode = 401;
    }
    
    public DemoiselleSecurityException(String string, int statusCode) {
        super(string);
        this.statusCode = statusCode;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void addMessage(String field, String msg) {

        messages.put(field, msg);
    }

    @Override
    public HashMap<String, String> getMessages() {
        return messages;
    }
}
