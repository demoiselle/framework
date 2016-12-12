/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.exception;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 *
 * Exception for all Multi Tenancy operations errors in Framework.
 *
 */
public class DemoiselleMultiTenancyException extends DemoiselleException {

	private static final long serialVersionUID = 1L;

	public DemoiselleMultiTenancyException() {
	}

	public DemoiselleMultiTenancyException(String message) {
		super(message);
	}

	public DemoiselleMultiTenancyException(Throwable cause) {
		super(cause);
	}

	public DemoiselleMultiTenancyException(String message, Throwable cause) {
		super(message, cause);
	}

}
