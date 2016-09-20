/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.basic.impl;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.Dependent;
import org.demoiselle.jee.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokensManagerImpl implements TokensManager {

    private static ConcurrentHashMap<String, Principal> repo = new ConcurrentHashMap<>();

    @Override
    public Principal getUser(String token) {
        return repo.get(token);
    }

    @Override
    public String create(Principal user) {
        String value = null;
        if (!repo.containsValue(user)) {
            value = UUID.randomUUID().toString();
            repo.put(value, user);
        }
        return value;
    }

    @Override
    public void remove(String token) {
        repo.remove(token);
    }

    @Override
    public boolean validate(String token) {
        return repo.containsKey(token);
    }

}
