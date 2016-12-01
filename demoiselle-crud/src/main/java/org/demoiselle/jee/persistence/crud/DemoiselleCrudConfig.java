/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 *
 * @author 70744416353
 */
@Configuration(prefix = "demoiselle.crud")
public class DemoiselleCrudConfig {

    private boolean patternsEnabled = true;
    private int acceptRange = 20;

    public int getAcceptRange() {
        return acceptRange;
    }

    public void setAcceptRange(int acceptRange) {
        this.acceptRange = acceptRange;
    }

    public boolean isPatternsEnabled() {
        return patternsEnabled;
    }

    public void setPatternsEnabled(boolean patternsEnabled) {
        this.patternsEnabled = patternsEnabled;
    }

}
