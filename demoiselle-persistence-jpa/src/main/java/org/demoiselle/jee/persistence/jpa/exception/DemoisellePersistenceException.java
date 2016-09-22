/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.exception;

import org.demoiselle.jee.core.exception.DemoiselleException;

public class DemoisellePersistenceException extends DemoiselleException {

	private static final long serialVersionUID = 1L;

	public DemoisellePersistenceException(String message) {
		super(message);
	}

	public DemoisellePersistenceException(Throwable cause) {
		super(cause);
	}

	public DemoisellePersistenceException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
