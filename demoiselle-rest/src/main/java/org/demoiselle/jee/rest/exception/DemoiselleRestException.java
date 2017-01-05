/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

import java.util.HashSet;

import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.exception.DemoiselleException;

public class DemoiselleRestException extends DemoiselleException {

	private static final long serialVersionUID = 519_965_615_171_844_237L;

	protected HashSet<DemoiselleRestExceptionMessage> messages = new HashSet<DemoiselleRestExceptionMessage>();

	protected int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();

	public DemoiselleRestException() {
	}

	public DemoiselleRestException(int statusCode) {
		this.statusCode = statusCode;
	}

	public DemoiselleRestException(String string) {
		super(string);
	}

	public DemoiselleRestException(String string, int statusCode) {
		super(string);
		this.statusCode = statusCode;
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
		messages.add(new DemoiselleRestExceptionMessage(field, msg, null));
	}

	public void addMessage(String error, String error_description, String error_link) {
		messages.add(new DemoiselleRestExceptionMessage(error, error_description, error_link));
	}

	public HashSet<DemoiselleRestExceptionMessage> getMessages() {
		return messages;
	}

	public void setMessages(HashSet<DemoiselleRestExceptionMessage> msgs) {
		this.messages = msgs;
	}

}
