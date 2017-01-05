/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source {@link ConfigurationType} for
 * the type of {@link String}.
 * 
 * <p>
 * Sample:
 * </p>
 * <p>
 * For the extraction of a string type of a properties file the statement made
 * in the properties will have the following format:
 * </p>
 * 
 * <pre>
 * demoiselle.ip=192.168.0.120
 * </pre>
 * 
 * And the configuration class will be declared as follows:
 * 
 * <pre>
 * 
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *     private String ip;
 *
 *     public String getIp() {
 *         return ip;
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
public class ConfigurationStringValueExtractor implements ConfigurationValueExtractor {

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try{
            return configuration.getString(prefix + key);
        }
        catch(Exception e){
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType() == String.class;
    }
}
