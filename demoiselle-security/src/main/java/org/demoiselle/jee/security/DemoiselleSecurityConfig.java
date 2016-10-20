/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 *
 * @author 70744416353
 */
@Configuration(resource = "demoiselle-security", prefix = "")
public class DemoiselleSecurityConfig {

    private boolean corsEnabled;

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

}
