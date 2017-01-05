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
import org.apache.commons.configuration2.DataConfiguration;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the type of {@link Object[]}.
 * 
 * <p>
 * Sample:
 * </p>
 * 
 * <p>
 * For the extraction of an array type of a properties file the statement made
 * in the properties will have the following format:
 * </p>
 * 
 * <pre>
 * demoiselle.intergerArray=-1
 * demoiselle.intergerArray=0
 * demoiselle.intergerArray=1
 * </pre>
 * 
 * And the configuration class will be declared as follows:
 * 
 * <pre>
 * 
 * &#64;Configuration
 * public class MyConfig {
 *     private Integer[] integerArray;
 * 
 *     public Integer[] getIntegerArray() {
 *         return this.integerArray;
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
public class ConfigurationArrayValueExtractor implements ConfigurationValueExtractor {

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try{
            return new DataConfiguration(configuration).getArray(field.getType().getComponentType(), prefix + key);
        }
        catch(Exception e){
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType().isArray();
    }
}
