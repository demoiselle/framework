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

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;
import org.apache.commons.lang3.ClassUtils;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * 
 * Adds the data extraction capability of a source ({@link ConfigurationType})
 * for the types:
 * 
 * <ul>
 * <li>{@link Boolean}</li>
 * <li>{@link Byte}</li>
 * <li>{@link Character}</li>
 * <li>{@link Short}</li>
 * <li>{@link Integer}</li>
 * <li>{@link Long}</li>
 * <li>{@link Double}</li>
 * <li>{@link Float}</li>
 * <li>{@link Void}</li>
 * </ul>
 * 
 * <p>
 * Sample:
 * </p>
 * 
 * <p>
 * For the extraction of a int type of a properties file the statement made in
 * the properties will have the following format:
 * </p>
 * 
 * <pre>
 * demoiselle.pageSize = 10
 * </pre>
 * 
 * And the configuration class will be declared as follows:
 * 
 * <pre>
 * 
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *     private int pageSize;
 *
 *     public String getPageSize() {
 *         return pageSize;
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
        try{
            return new DataConfiguration(configuration).get(ClassUtils.primitiveToWrapper(field.getType()), prefix + key);
        }
        catch(Exception e){
            throw new DemoiselleConfigurationValueExtractorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported(Field field) {
        return field.getType().isPrimitive() || wrappers.contains(field.getType());
    }
}
