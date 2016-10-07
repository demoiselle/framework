/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.token.impl;

import java.io.Serializable;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.core.annotation.Name;

/**
 *
 * @author 70744416353
 */
@Configuration(resource = "demoiselle-security-token", prefix = "")
public class Config implements Serializable {

    private static final long serialVersionUID = 638435989235076782L;

    @Name("type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
