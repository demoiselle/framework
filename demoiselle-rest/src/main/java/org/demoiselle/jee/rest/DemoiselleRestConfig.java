/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest;

import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.core.annotation.Name;

/**
 *
 * @author 70744416353
 */
@Configuration(resource = "demoiselle-rest", prefix = "")
public class DemoiselleRestConfig {

    private boolean corsEnabled;
    private boolean gzipEnabled;

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public void setGzipEnabled(boolean gzipEnabled) {
        this.gzipEnabled = gzipEnabled;
    }

}
