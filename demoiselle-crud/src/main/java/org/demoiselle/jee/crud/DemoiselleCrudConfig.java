/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.crud;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configurations of REST module.
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.crud")
public class DemoiselleCrudConfig {

    private String logicalDeleteField;
    private String ownerField;

    public String getLogicalDeleteField() {
        return logicalDeleteField;
    }

    public void setLogicalDeleteField(String logicalDeleteField) {
        this.logicalDeleteField = logicalDeleteField;
    }

    public String getOwnerField() {
        return ownerField;
    }

    public void setOwnerField(String ownerField) {
        this.ownerField = ownerField;
    }

}
