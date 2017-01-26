/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.model;

import java.util.Map;

import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * 
 * @author SERPRO
 *
 */
@Configuration(resource = "app", type = ConfigurationType.PROPERTIES, prefix = "demoiselle.configuration")
public class ConfigMapModel {

    private Map<String, String> configMap;

    public Map<String, String> getConfigMap() {
        return configMap;
    }
    
}