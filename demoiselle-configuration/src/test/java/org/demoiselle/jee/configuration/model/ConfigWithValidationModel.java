/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * 
 * @author SERPRO
 *
 */
@Configuration()
public class ConfigWithValidationModel {

    @NotNull
    private String configString;

    @Null
    private Integer configInteger;

    public String getConfigString() {
        return configString;
    }

    public Integer getConfigInteger() {
        return configInteger;
    }

}
