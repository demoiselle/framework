/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * 
 * Messages used to inform user
 *
 * @author SERPRO
 */
@MessageBundle
//TODO rever nome dos parametros
public interface ConfigurationMessage {

	@MessageTemplate("{load-configuration-class}")
	String loadConfigurationClass(String name);

	@MessageTemplate("{configuration-name-attribute-cant-be-empty}")
	String configurationNameAttributeCantBeEmpty();

	@MessageTemplate("{file-not-found}")
	String fileNotFound(String resource);

	@MessageTemplate("{configuration-dot-after-prefix}")
	String configurationDotAfterPrefix(String resource);

	@MessageTemplate("{configuration-key-not-found}")
	String configurationKeyNotFoud(String string);

	@MessageTemplate("{configuration-field-loaded}")
	String configurationFieldLoaded(String string, Object object);

	@MessageTemplate("{configuration-not-conversion}")
	String configurationNotConversion(String field, String type);

	@MessageTemplate("{configuration-generic-extraction-error}")
	String configurationGenericExtractionError(String string, String canonicalName);

	@MessageTemplate("{configuration-extractor-not-found}")
	String configurationExtractorNotFound(String genericString, String name);

	@MessageTemplate("{ambiguous-strategy-resolution}")
	String ambigousStrategyResolution(String canonicalName, String string);

	@MessageTemplate("{configuration-error-get-value}")
	String configurationErrorGetValue(String string, Object object);

	@MessageTemplate("{configuration-error-set-value}")
	String configurationErrorSetValue(Object value, Object field, Object object);
	

}
