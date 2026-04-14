/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationPlaceholderResolver;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for primitive and wrapper types.
 *
 * @author SERPRO
 */
@Dependent
public class ConfigurationPrimitiveOrWrapperValueExtractor implements ConfigurationValueExtractor {

    private static final Set<Object> wrappers = new HashSet<>();

    static {
        wrappers.add(Boolean.class);
        wrappers.add(Byte.class);
        wrappers.add(Character.class);
        wrappers.add(Short.class);
        wrappers.add(Integer.class);
        wrappers.add(Long.class);
        wrappers.add(Double.class);
        wrappers.add(Float.class);
        wrappers.add(Void.TYPE);
    }

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
        return field.getType().isPrimitive() || wrappers.contains(field.getType());
    }
}
