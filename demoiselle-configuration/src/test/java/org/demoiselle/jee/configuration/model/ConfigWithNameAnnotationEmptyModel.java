/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.model;

import org.demoiselle.jee.configuration.annotation.ConfigurationName;

/**
 * 
 * @author SERPRO
 *
 */
public class ConfigWithNameAnnotationEmptyModel {

    @ConfigurationName
    public String configString;

    public String getConfigString() {
        return configString;
    }

}
