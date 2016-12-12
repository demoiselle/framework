/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

/**
 * Exception class intended to be used by framework configuration and to be derived by other framework exceptions.
 * 
 * @author SERPRO
 */
public class DemoiselleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor .
	 * 
	 */
	public DemoiselleException() {

	}
	
	/**
	 * Constructor with message.
	 * 
	 * @param message
	 *            exception message
	 */
	public DemoiselleException(String message) {
		super(message);
	}

	/**
	 * Constructor with cause.
	 * 
	 * @param cause
	 *            exception cause
	 */
	public DemoiselleException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with message and cause.
	 * 
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public DemoiselleException(String message, Throwable cause) {
		super(message, cause);
	}
}
