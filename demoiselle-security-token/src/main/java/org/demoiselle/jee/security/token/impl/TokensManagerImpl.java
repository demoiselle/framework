/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.token.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokensManagerImpl implements TokensManager {

    private static ConcurrentHashMap<String, DemoisellePrincipal> repo = new ConcurrentHashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private Token token;

    @Override
    public DemoisellePrincipal getUser() {
        if (token.getKey() != null && !token.getKey().isEmpty()) {
            return repo.get(token.getKey());
        }
        return null;
    }

    @Override
    public void setUser(DemoisellePrincipal user) {
        if (!repo.containsValue(user)) {
            String value = UUID.randomUUID().toString();
            repo.put(value, user);
            token.setKey(value);
        } else {
            token.setKey((repo.entrySet().parallelStream().filter((e) -> (user.equals(e.getValue()))).findAny().get()).getKey());
        }
        token.setType("Token");
    }

    @Override
    public boolean validate() {
        return getUser() != null;
    }

}
