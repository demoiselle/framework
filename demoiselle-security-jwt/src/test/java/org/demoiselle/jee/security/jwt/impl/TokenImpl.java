/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import org.demoiselle.jee.core.api.security.Token;

/**
 *
 * @author 70744416353
 */
@Default
@ApplicationScoped
public class TokenImpl implements Token {

    @Override
    public String getKey() {
        return "";
    }

    @Override
    public void setKey(String key) {

    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public void setType(String type) {

    }

}
