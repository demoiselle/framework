/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.api.security;

/**
 *
 * @author 70744416353
 */
public interface Token {

    /**
     * @return Key name
     */
    public String getKey();

    /**
     *
     * @param key Key name
     */
    public void setKey(String key);

    /**
     * @return Type name
     */
    public String getType();

    /**
     *
     * @param type Type name
     */
    public void setType(String type);
}
