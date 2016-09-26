/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.basic.impl;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokensManagerImpl implements TokensManager {

    private static ConcurrentHashMap<String, Principal> repo = new ConcurrentHashMap<>();

    @Inject
    private Logger logger;

    @Inject
    @RequestScoped
    private Token token;

    @Inject
    @RequestScoped
    private Principal loggedUser;

    @Override
    public Principal getUser() {
        if (loggedUser == null) {
            if (token.getKey() != null && !token.getKey().isEmpty()) {
                loggedUser = repo.get(token.getKey());
                return loggedUser;
            }
        }
        return loggedUser;
    }

    @Override
    public void setUser(Principal user) {
        String value = null;
        if (!repo.containsValue(user)) {
            value = UUID.randomUUID().toString();
            repo.put(value, user);
            token.setKey(value);
            token.setType("Basic");
        }
    }

    @Override
    public boolean validate() {
        return true;//(getUser() != null && repo.get(token.getKey()).);
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public void setToken(Token token) {
        String key = null;
        if (repo.containsKey(token.getKey())) {
            loggedUser = repo.get(key);
        }
    }

    @Override
    public void setRoles(List<String> roles) {
        
    }

    @Override
    public void setPermissions(Map<String, String> permissions) {
        
    }

}
