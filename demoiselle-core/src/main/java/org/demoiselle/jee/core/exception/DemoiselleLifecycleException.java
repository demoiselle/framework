/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

/**
 * 
 * Represents a throw Exceptions from Lifecycle feature
 * 
 * @author SERPRO
 *
 */
public class DemoiselleLifecycleException extends DemoiselleException {

	private static final long serialVersionUID = -3751898389838825583L;

	public DemoiselleLifecycleException(Exception e) {
		super(e);
	}

}
