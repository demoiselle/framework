/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * 
 * Represents a throw Exceptions from Lifecycle feature
 * 
 * @author SERPRO
 *
 */
public class LifecycleException extends DemoiselleException {

	private static final long serialVersionUID = -3751898389838825583L;

	public LifecycleException(Exception e) {
		super(e);
	}

}
