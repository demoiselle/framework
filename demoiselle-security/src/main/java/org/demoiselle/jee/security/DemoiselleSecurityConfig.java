/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security;

import java.util.HashMap;
import java.util.Map;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security")
public class DemoiselleSecurityConfig {

    private boolean corsEnabled;
    private boolean hashcashEnabled;
    private long wait;

    private final Map<String, String> paramsHeaderSecuriry = new HashMap<>();
    private final Map<String, String> paramsHeaderCors = new HashMap<>();

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public Map<String, String> getParamsHeaderSecuriry() {
        return paramsHeaderSecuriry;
    }

    public Map<String, String> getParamsHeaderCors() {
        return paramsHeaderCors;
    }

    public boolean isHashcashEnabled() {
        return hashcashEnabled;
    }

    public long getWait() {
        return wait;
    }

}
