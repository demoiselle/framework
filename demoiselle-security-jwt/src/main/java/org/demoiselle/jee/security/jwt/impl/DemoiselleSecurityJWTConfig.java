/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;
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

}
