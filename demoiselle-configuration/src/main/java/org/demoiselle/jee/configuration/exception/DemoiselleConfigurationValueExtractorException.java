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
 * Exception that handles errors on extracting process
 * 
 * @author SERPRO
 */
public class DemoiselleConfigurationValueExtractorException extends DemoiselleException {

    private static final long serialVersionUID = 1L;

    public DemoiselleConfigurationValueExtractorException(String message) {
        super(message);
    }

    public DemoiselleConfigurationValueExtractorException(String message, Throwable cause) {
        super(message, cause);
    }
}
