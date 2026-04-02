/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.security.event.AuthenticationEvent;

/**
 * <p>
 * It manages the security features and serves as a hub for specific 
 * imlementations, look at JWT and Token
 * </p>
 *
 * @see <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 *
 * @author SERPRO
 */
@RequestScoped
public class SecurityContextImpl implements SecurityContext {

    @Inject
    private TokenManager tm;

    @Inject
    private Event<AuthenticationEvent> authEvent;

    @Override
    public boolean hasPermission(String resource, String operation) {
        if (getUser() == null) return false;

        List<String> list = getUser().getPermissions().get(resource);

        if (list != null && !list.isEmpty()) {
            return list.contains(operation);
        }

        return false;
    }

    @Override
    public boolean hasRole(String role) {
        if (getUser() == null) return false;
        return getUser().getRoles().contains(role);
    }

    @Override
    public boolean isLoggedIn() {
        try {
            if (tm == null) return false;
            return tm.validate();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public DemoiselleUser getUser() {
        if (tm == null) return null;
        return tm.getUser();
    }

    @Override
    public void setUser(DemoiselleUser loggedUser) {
        tm.setUser(loggedUser);
        authEvent.fire(new AuthenticationEvent(loggedUser, AuthenticationEvent.Action.LOGIN));
    }

    @Override
    public DemoiselleUser getUser(String issuer, String audience) {
        return tm.getUser(issuer, audience);
    }

    @Override
    public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {
        tm.setUser(loggedUser, issuer, audience);
        authEvent.fire(new AuthenticationEvent(loggedUser, AuthenticationEvent.Action.LOGIN));
    }

    @Override
    public void removeUser(DemoiselleUser loggedUser) {
        tm.removeUser(loggedUser);
        authEvent.fire(new AuthenticationEvent(loggedUser, AuthenticationEvent.Action.LOGOUT));
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) return false;
        if (getUser() == null) return false;
        for (String role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    @Override
    public boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) return false;
        if (getUser() == null) return false;
        for (String role : roles) {
            if (!hasRole(role)) return false;
        }
        return true;
    }

}
