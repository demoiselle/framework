/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.impl;

import javax.enterprise.context.Dependent;
import org.demoiselle.jee.core.interfaces.security.Token;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokenImpl implements Token {

    private String key;
    private String type;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

}
