/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.message;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;

/**
 * Message class intended to be used by REST module.
 * 
 * @author SERPRO
 */
@MessageBundle
public interface DemoiselleRESTMessage {

	@MessageTemplate("{unhandled-database-exception}")
	String unhandledDatabaseException();

	@MessageTemplate("{unhandled-malformed-input-output-exception}")
	String unhandledMalformedInputOutputException();

	@MessageTemplate("{http-exception}")
	String httpException();

	@MessageTemplate("{unhandled-server-exception}")
	String unhandledServerException();

}