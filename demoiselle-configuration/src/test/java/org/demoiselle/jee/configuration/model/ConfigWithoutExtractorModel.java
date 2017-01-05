/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.model;

import java.util.Set;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * 
 * @author SERPRO
 *
 */
@Configuration(resource = "app-test", prefix = "")
public class ConfigWithoutExtractorModel {

    private Set<?> field;

    public Set<?> getField() {
        return field;
    }

}
