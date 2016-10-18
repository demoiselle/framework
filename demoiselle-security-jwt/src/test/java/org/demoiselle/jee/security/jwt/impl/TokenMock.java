/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import org.demoiselle.jee.core.api.security.Token;

/**
 *
 * @author 70744416353
 */
@Default
@ApplicationScoped
public class TokenMock implements Token {

    private String key;
    private String type;

    /**
     *
     * @return
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key
     */
    @Override
    public void setKey(String key) {
        this.key = key;
    }

    /**
     *
     * @return
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TokenMock other = (TokenMock) obj;
        return Objects.equals(this.key, other.key);
    }

    @Override
    public String toString() {
        return "Token{" + "\"key\"=" + key + ", \"type\"=" + type + '}';
    }

}
