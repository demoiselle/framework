/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import jakarta.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationPlaceholderResolver;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the type of {@link Enum}.
 *
 * @author SERPRO
 */
@Dependent
public class ConfigurationEnumValueExtractor implements ConfigurationValueExtractor {

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try {
            return ConfigurationPlaceholderResolver.convert(configuration.getString(prefix + key), field.getType());
        } catch (Exception e) {
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType().isEnum();
    }
}
