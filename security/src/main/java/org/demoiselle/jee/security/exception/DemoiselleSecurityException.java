/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import java.util.HashMap;

import org.demoiselle.jee.core.exception.DemoiselleException;

public class DemoiselleSecurityException extends DemoiselleException {

	private static final long serialVersionUID = 519965615171844237L;

	private HashMap<String, String> messages = new HashMap<String, String>();

	private int statusCode;

	public DemoiselleSecurityException(String string) {
		super(string);
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
