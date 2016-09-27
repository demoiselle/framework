/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

/**
 * <p>
 * Thrown when trying to access some resource or execute an operation that requires authentication.
 * </p>
 *
 * @author SERPRO
 */
public class NotLoggedInException extends DemoiselleSecurityException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>
	 * Constructs an <code>NotLoggedInException</code> with a message.
	 * </p>
	 *
	 * @param message exception message
	 */
	public NotLoggedInException(String message) {
		super(message);
	}
        
        
}
