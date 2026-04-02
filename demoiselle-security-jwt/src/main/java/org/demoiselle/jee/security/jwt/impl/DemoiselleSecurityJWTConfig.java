/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security.jwt")
public class DemoiselleSecurityJWTConfig implements Serializable {

    private static final long serialVersionUID = 638_435_989_235_076_782L;

    private String type;

    @ConfigurationSuppressLogger
    private String privateKey;

    private String publicKey;

    private Long timetoLiveMilliseconds;

    private String issuer;

    private String audience;

    private String algorithmIdentifiers;

    private Long refreshTokenTtlMilliseconds;

    private String allowedAlgorithms;

    private Integer clockSkewSeconds;

    private String activeKeyId;

    public String getType() {
        return type;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Long getTimetoLiveMilliseconds() {
        return timetoLiveMilliseconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public String getAlgorithmIdentifiers() {
        return algorithmIdentifiers;
    }

    public Long getRefreshTokenTtlMilliseconds() {
        return refreshTokenTtlMilliseconds != null ? refreshTokenTtlMilliseconds : 86400000L;
    }

    public String getAllowedAlgorithms() {
        return allowedAlgorithms != null ? allowedAlgorithms : algorithmIdentifiers;
    }

    public Integer getClockSkewSeconds() {
        int value = clockSkewSeconds != null ? clockSkewSeconds : 60;
        if (value < 0) {
            return 60;
        }
        return value;
    }

    public String getActiveKeyId() {
        return activeKeyId;
    }

    /**
     * Returns the allowed algorithms as a List of Strings.
     * Falls back to algorithmIdentifiers if allowedAlgorithms is not configured.
     */
    public List<String> getAllowedAlgorithmsList() {
        String algorithms = getAllowedAlgorithms();
        if (algorithms == null || algorithms.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(algorithms.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
