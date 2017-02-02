/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.Priorities.AUTHENTICATION;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;

/**
 *
 * @author SERPRO
 */
@ApplicationScoped
@Priority(AUTHENTICATION)
public class TokenManagerMock implements TokenManager {

    private final ConcurrentHashMap<String, DemoiselleUser> repo = new ConcurrentHashMap<>();

    @Inject
    private Token token;

    /**
     *
     * @return
     */
    @Override
    public DemoiselleUser getUser() {
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
    public void setUser(DemoiselleUser user) {
        token.setKey(null);

        repo.entrySet().stream().filter((entry) -> (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity()))).forEach((entry) -> {
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
     *
     * @return
     */
    @Override
    public void setUser(DemoiselleUser user, String issuer, String audience) {
        user.addParam("issuer", issuer);
        user.addParam("audience", audience);
        setUser(user);
    }

    @Override
    public boolean validate() {
        return getUser() != null;
    }

    @Override
    public boolean validate(String issuer, String audience) {
        if (validate()) {
            return (getUser().getParams().containsValue(issuer) && getUser().getParams().containsValue(audience));
        } else {
            return false;
        }
    }

    public void removeToken() {
        repo.remove(token.getKey());
    }

    @Override
    public void removeUser(DemoiselleUser user) {
        repo.entrySet().stream().filter((entry) -> (entry.getValue().getIdentity().equalsIgnoreCase(user.getIdentity()))).parallel().forEach((entry) -> {
            token.setKey(entry.getKey());
        });
        removeToken();
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

}
