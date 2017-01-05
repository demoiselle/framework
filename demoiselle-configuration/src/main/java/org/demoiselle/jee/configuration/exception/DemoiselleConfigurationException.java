/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.exception;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * 
 * Main exception Demoiselle Configuration feature
 * 
 * @author SERPRO
 */
public class DemoiselleConfigurationException extends DemoiselleException {

    private static final long serialVersionUID = 1L;

    public DemoiselleConfigurationException(String message) {
        super(message);
    }

    public DemoiselleConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
