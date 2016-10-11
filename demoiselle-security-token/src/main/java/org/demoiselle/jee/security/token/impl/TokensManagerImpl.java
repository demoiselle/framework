/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.token.impl;

import static java.util.UUID.randomUUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class TokensManagerImpl implements TokensManager {

    private ConcurrentHashMap<String, DemoisellePrincipal> repo = new ConcurrentHashMap<>();

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
        token.setKey(null);

        repo.entrySet().stream().parallel().filter((entry) -> (entry.getValue().equals(user))).forEach((entry) -> {
            token.setKey(entry.getKey());
        });

        if (token.getKey() == null) {
            String value = randomUUID().toString();
            repo.putIfAbsent(value, user.clone());
            token.setKey(value);
        }

        token.setType("Token");
    }

    @Override
    public boolean validate() {
        return getUser() != null;
    }

}
