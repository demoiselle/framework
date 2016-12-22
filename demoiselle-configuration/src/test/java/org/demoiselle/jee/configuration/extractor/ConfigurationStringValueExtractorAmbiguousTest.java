/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor;

/**
 * 
 * @author SERPRO
 *
 */
@Dependent
public class ConfigurationStringValueExtractorAmbiguousTest implements ConfigurationValueExtractor{

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
		return new ConfigurationStringValueExtractor().getValue(prefix, key, field, configuration);
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType() == String.class;
	}

	
}
