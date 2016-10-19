package org.demoiselle.jee.security.token.impl;

import java.util.Map;
import static java.util.UUID.randomUUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.AUTHENTICATION;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@ApplicationScoped
@Priority(AUTHENTICATION)
public class TokensManagerImpl implements TokensManager {

    private ConcurrentHashMap<String, DemoisellePrincipal> repo = new ConcurrentHashMap<>();

    @Inject
    private Token token;

    /**
     *
     * @return
     */
    @Override
    public DemoisellePrincipal getUser() {
        if (token.getKey() != null && !token.getKey().isEmpty()) {
            return repo.get(token.getKey());
        }
        return null;
    }

    /**
     *
     * @param user
     */
    @Override
    public void setUser(DemoisellePrincipal user) {
        token.setKey(null);

        for (Map.Entry<String, DemoisellePrincipal> entry : repo.entrySet()) {
            if (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity())) {
                token.setKey(entry.getKey());
            }
        }

        if (token.getKey() == null) {
            String value = randomUUID().toString();
            repo.putIfAbsent(value, user.clone());
            token.setKey(value);
        }

        token.setType("Token");
    }

    /**
     *
     * @return
     */
    @Override
    public boolean validate() {
        return getUser() != null;
    }

    public void removeToken() {
        repo.remove(token.getKey());
    }

    public void removeToken(DemoisellePrincipal user) {
        for (Map.Entry<String, DemoisellePrincipal> entry : repo.entrySet()) {
            if (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity())) {
                token.setKey(entry.getKey());
            }
        }
        removeToken();
    }

}
