/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.interfaces.security;

/**
 *
 * @author 70744416353
 */
public interface Token {

    public String getKey();

    public void setKey(String key);

    public String getType();

    public void setType(String type);
}
