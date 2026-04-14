/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationPlaceholderResolver;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the type of {@link Map}.
 *
 * @author SERPRO
 */
@Dependent
public class ConfigurationMapValueExtractor implements ConfigurationValueExtractor {

    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try {
            Map<String, Object> value = null;

            String regexp = "^(" + prefix + ")(" + key + ")(\\.([^=]*))?$";
            Pattern pattern = patternCache.computeIfAbsent(regexp, Pattern::compile);

            for (Iterator<String> iter = configuration.getKeys(); iter.hasNext();) {
                String iterKey = iter.next();
                Matcher matcher = pattern.matcher(iterKey);

                if (matcher.matches()) {
                    String confKey = matcher.group(1) + matcher.group(2)
                            + (matcher.group(3) != null ? matcher.group(3) : "");

                    if (value == null) {
                        value = new HashMap<>();
                    }

                    String mapKey = matcher.group(4) == null ? "default" : matcher.group(4);
                    value.putIfAbsent(mapKey, ConfigurationPlaceholderResolver.resolve(configuration.getString(confKey)));
                }
            }

            return value;
        } catch (Exception e) {
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType() == Map.class;
    }
}
