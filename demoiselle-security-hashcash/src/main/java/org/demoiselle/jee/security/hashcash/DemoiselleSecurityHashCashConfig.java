package org.demoiselle.jee.security.hashcash;

import java.io.Serializable;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security.hashcash")
public class DemoiselleSecurityHashCashConfig implements Serializable {

    @ConfigurationSuppressLogger
    private String hashcashKey;

    private Long timetoLiveMilliseconds;

    public String getHashcashKey() {
        return hashcashKey;
    }

    public Long getTimetoLiveMilliseconds() {
        return timetoLiveMilliseconds;
    }

}
