/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.exception;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * 
 * Main exception Demoiselle Script feature
 * 
 * @author SERPRO
 */
public class DemoiselleScriptException extends DemoiselleException{

	private static final long serialVersionUID = 1L;

	public DemoiselleScriptException(String message) {
		super(message);
	}
	
	public DemoiselleScriptException(String message, Throwable cause) {
		super(message, cause);
	}
}