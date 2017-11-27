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

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the type of {@link Map}.
 *
 * <p>
 * Sample:
 * </p>
 *
 * <p>
 * For the extraction of a {@link Map} type of a properties file the statement
 * made in the properties will have the following format:
 * </p>
 *
 * <pre>
 * demoiselle.connectionConfiguration.ip=192.168.0.120
 * demoiselle.connectionConfiguration.gateway=192.168.0.1
 * demoiselle.connectionConfiguration.dns1=200.10.128.99
 * demoiselle.connectionConfiguration.dns2=200.10.128.88
 * </pre>
 *
 * And the configuration class will be declared as follows:
 *
 * <pre>
 *
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *     private Map&#60;String, String&#62; connectionConfiguration;
 *
 *     public Map&#60;String, String&#62; getConnectionConfiguration() {
 *         return connectionConfiguration;
 *     }
 *
 * }
 *
 * </pre>
 *
 * @author SERPRO
 *
 */
@Dependent
public class ConfigurationMapValueExtractor implements ConfigurationValueExtractor {

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try {
            Map<String, Object> value = null;

            String regexp = "^(" + prefix + ")(" + key + ")(\\.([^=]*))?$";
            Pattern pattern = Pattern.compile(regexp);

            for (Iterator<String> iter = configuration.getKeys(); iter.hasNext();) {
                String iterKey = iter.next();
                Matcher matcher = pattern.matcher(iterKey);

                if (matcher.matches()) {
                    String confKey = matcher.group(1) + matcher.group(2)
                            + (matcher.group(3) != null ? matcher.group(3) : "");

                    if (value == null) {
                        value = new ConcurrentHashMap<>();
                    }

                    String mapKey = matcher.group(4) == null ? "default" : matcher.group(4);
                    value.putIfAbsent(mapKey, configuration.getString(confKey));
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
