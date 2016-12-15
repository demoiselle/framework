/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;

import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.SuppressConfigurationLogger;
import org.demoiselle.jee.core.annotation.Name;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security.jwt")
public class DemoiselleSecurityJWTConfig implements Serializable {

    private static final long serialVersionUID = 638_435_989_235_076_782L;

    @Name("type")
    private String type;

    @Name("privateKey")
    @SuppressConfigurationLogger
    private String privateKey;

    @Name("publicKey")
    private String publicKey;

    @Name("timetoLiveMilliseconds")
    private Long timetoLiveMilliseconds;

    @Name("issuer")
    private String issuer;

    @Name("audience")
    private String audience;
    
    @Name("algorithmIdentifiers")
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
