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
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the type of {@link Enum}.
 * 
 * <p>
 * Sample:
 * </p>
 * 
 * <p>
 * For the extraction of an Enum type of a properties file the statement made in
 * the properties will have the following format:
 * </p>
 * 
 * <pre>
 * demoiselle.loadedConfigurationType = SYSTEM
 * </pre>
 * 
 * And the configuration class will be declared as follows:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *  private {@link ConfigurationType} loadedConfigurationType;
 *
 *  public ConfigType getLoadedConfigurationType(){
 *    return loadedConfigurationType;
 *  }
 *
 * }
 * 
 * </pre>
 * 
 * @author SERPRO
 * 
 */
@Dependent
public class ConfigurationEnumValueExtractor implements ConfigurationValueExtractor {

    @Override
    public Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException {
        try{
            String value = configuration.getString(prefix + key);
    
            if (value != null && !"".equals(value.trim())) {
                Object enums[] = field.getType().getEnumConstants();
    
                for (int i = 0; i < enums.length; i++) {
                    if (((Enum<?>) enums[i]).name().equals(value)) {
                        return enums[i];
                    }
                }
            }
    
            return null;
        }
        catch(Exception e){
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }

    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType().isEnum();
    }

}
