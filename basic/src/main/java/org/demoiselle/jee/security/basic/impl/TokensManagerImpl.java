/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.basic.impl;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.demoiselle.jee.security.Token;
import org.demoiselle.jee.security.interfaces.TokensManager;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokensManagerImpl implements TokensManager {

    private static ConcurrentHashMap<String, Principal> repo = new ConcurrentHashMap<>();

    @Inject
    private Logger logger;

    @Override
    public Principal getUser(Token token) {
        return repo.get(token.getKey());
    }

    @Override
    public Token getToken(Principal user) {
        String value = null;
        if (!repo.containsValue(user)) {
            value = UUID.randomUUID().toString();
            repo.put(value, user);
        } else {
            for (Map.Entry<String, Principal> entry : repo.entrySet()) {
                if (entry.getValue().equals(user)) {
                    return entry.getKey();
                }
            }
        }
        return value;
    }

}
