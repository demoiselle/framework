/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.token.impl;

import static java.util.UUID.randomUUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.AUTHENTICATION;
import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;

/**
 *
 * @author SERPRO
 */
@RequestScoped
@Priority(AUTHENTICATION)
public class TokenManagerImpl implements TokenManager {

    private static final ConcurrentHashMap<String, DemoiselleUser> repo = new ConcurrentHashMap<>();

    @Inject
    private Token token;

    /**
     * Returns the user that is stored in a list in memory, from the token sent
     * in http header
     *
     * @return org.demoiselle.jee.core.api.security.DemoiselleUser
     */
    @Override
    public DemoiselleUser getUser() {
        if (token.getKey() != null && !token.getKey().isEmpty()) {
            return repo.get(token.getKey());
        }
        return null;
    }

    @Override
    public DemoiselleUser getUser(String issuer, String audience) {
        DemoiselleUser dml = getUser();
        if (dml != null) {
            if (dml.getParams().containsValue(issuer) && dml.getParams().containsValue(audience)) {
                return dml;
            }
        }
        return null;
    }

    /**
     * It will be included in the user memory and generate a unique
     * identification token to be placed in the header of HTTP requests
     *
     * @param user org.demoiselle.jee.core.api.security.DemoiselleUser
     */
    @Override
    public void setUser(DemoiselleUser user) {
        token.setKey(null);

        repo.entrySet().stream().filter((entry) -> (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity()))).parallel().forEach((entry) -> {
            token.setKey(entry.getKey());
        });

        if (token.getKey() == null) {
            String value = randomUUID().toString();
            repo.putIfAbsent(value, user.clone());
            token.setKey(value);
        }
        token.setType(TokenType.TOKEN);
    }

    /**
     * It will be included in the user memory and generate a unique
     * identification token to be placed in the header of HTTP requests
     *
     * @param user org.demoiselle.jee.core.api.security.DemoiselleUser
     * @param issuer
     * @param audience
     */
    @Override
    public void setUser(DemoiselleUser user, String issuer, String audience) {
        user.addParam("issuer", issuer);
        user.addParam("audience", audience);
        setUser(user);
    }

    /**
     * validate the token and the user is in memory
     *
     * @return boolean
     */
    @Override
    public boolean validate() {
        return getUser() != null;
    }

    /**
     * validate the token and the user is in memory
     *
     * @return boolean
     */
    @Override
    public boolean validate(String issuer, String audience) {
        return getUser() != null;
    }

    /**
     * remove the token and the user is in memory
     */
    public void removeToken() {
        repo.remove(token.getKey());
        token.setKey(null);
    }

    /**
     * remove the token and the user is in memory
     *
     * @param user principal
     */
    @Override
    public void removeUser(DemoiselleUser user) {
        repo.entrySet().stream().filter((entry) -> (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity()))).parallel().forEach((entry) -> {
            token.setKey(entry.getKey());
        });
        removeToken();
    }

}
