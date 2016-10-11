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
     *
     * @return
     */
    public String getKey();

    /**
     *
     * @param key
     */
    public void setKey(String key);

    /**
     *
     * @return
     */
    public String getType();

    /**
     *
     * @param type
     */
    public void setType(String type);
}
