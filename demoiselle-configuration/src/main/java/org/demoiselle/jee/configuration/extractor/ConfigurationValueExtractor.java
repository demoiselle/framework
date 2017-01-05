/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;

/**
 * Definition interface to extractors.
 *
 * @author SERPRO
 * 
 */
public interface ConfigurationValueExtractor {

    /**
     * Extracts the value of a source based on the parameters.
     * 
     * @param prefix
     *            Prefix used in the source file.
     * @param key
     *            Key used in the source file.
     * @param field
     *            Field to be filled.
     * @param configuration
     *            {@link org.apache.commons.configuration2.Configuration} object
     *            responsible for extracting the value.
     * @return Object with the value set at source
     * @throws DemoiselleConfigurationValueExtractorException
     *             Exception issued if an error occurs.
     */
    Object getValue(String prefix, String key, Field field, Configuration configuration) throws DemoiselleConfigurationValueExtractorException;

    /**
     * Verify the type supported by the extractor
     * 
     * @param field
     *            Field to be validated.
     * @return True if supported and False if it is not supported
     */
    boolean isSupported(Field field);
}
