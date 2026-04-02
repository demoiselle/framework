/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.openapi;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configuration class for the OpenAPI module.
 *
 * <p>Maps the property {@code demoiselle.openapi.enabled} (default {@code true}).
 * When set to {@code false}, {@link DemoiselleOASModelReader#buildModel()}
 * returns an empty {@link org.eclipse.microprofile.openapi.models.OpenAPI} document.</p>
 */
@Configuration(prefix = "demoiselle.openapi")
public class OpenAPIConfig {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
