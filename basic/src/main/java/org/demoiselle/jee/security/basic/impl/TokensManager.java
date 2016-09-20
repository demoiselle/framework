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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 *
 * @author 70744416353
 */
@ApplicationScoped
public class TokensManager {

    private static ConcurrentHashMap<String, Principal> repo = new ConcurrentHashMap<>();

    @Inject
    private Logger logger;

    public Principal getUser(String token) {
        return repo.get(token);
    }

    public String getToken(Principal user) {
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

    public void remove(String token) {
        repo.remove(token);
    }

    public boolean validate(String token) {
        return repo.containsKey(token);
    }

}
