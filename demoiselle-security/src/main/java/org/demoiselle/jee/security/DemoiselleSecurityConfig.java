/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security")
public class DemoiselleSecurityConfig {

    private boolean corsEnabled;

    private final Map<String, String> paramsHeaderSecuriry = new ConcurrentHashMap<>();
    private final Map<String, String> paramsHeaderCors = new ConcurrentHashMap<>();

    private String[] corsAllowedOrigins = {"*"};
    private String[] corsAllowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
    private String[] corsAllowedHeaders = {"Content-Type", "Authorization"};
    private boolean corsAllowCredentials;
    private int corsMaxAge = 3600;

    private int bruteForceMaxAttempts = 5;
    private int bruteForceLockoutDuration = 300; // segundos

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public Map<String, String> getParamsHeaderSecuriry() {
        return paramsHeaderSecuriry;
    }

    public Map<String, String> getParamsHeaderCors() {
        return paramsHeaderCors;
    }

    public List<String> getCorsAllowedOrigins() {
        return Arrays.asList(corsAllowedOrigins);
    }

    public List<String> getCorsAllowedMethods() {
        return Arrays.asList(corsAllowedMethods);
    }

    public List<String> getCorsAllowedHeaders() {
        return Arrays.asList(corsAllowedHeaders);
    }

    public boolean isCorsAllowCredentials() {
        return corsAllowCredentials;
    }

    public int getCorsMaxAge() {
        return corsMaxAge > 0 ? corsMaxAge : 3600;
    }

    public int getBruteForceMaxAttempts() {
        return bruteForceMaxAttempts;
    }

    public int getBruteForceLockoutDuration() {
        return bruteForceLockoutDuration;
    }

}
