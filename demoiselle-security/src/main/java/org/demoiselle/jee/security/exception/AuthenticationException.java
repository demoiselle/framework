/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

/**
 * <p>
 * Thrown when the mecanism responsible for the entire authentication lifecycle fails.
 * </p>
 *
 * @author SERPRO
 */
public class AuthenticationException extends SecurityException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>
	 * Constructor with message.
	 * </p>
	 *
	 * @param message exception message
	 */
	public AuthenticationException(String message) {
		super(message);
	}

	/**
	 * <p>
	 * Constructor with the cause.
	 * </p>
	 *
	 * @param cause exception cause
	 */
	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>
	 * Constructor with message and cause.
	 * </p>
	 *
	 * @param message exception message
	 * @param cause exception cause
	 */
	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
