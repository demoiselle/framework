/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.TokenManager;

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

    @Override
    public boolean hasPermission(String resource, String operation) {

        List<String> list = getUser().getPermissions().get(resource);

        if (list != null && !list.isEmpty()) {
            return list.contains(operation);
        }

        return false;
    }

    @Override
    public boolean hasRole(String role) {
        return getUser().getRoles().contains(role);
    }

    @Override
    public boolean isLoggedIn() {
        return tm.validate();
    }

    @Override
    public DemoiselleUser getUser() {
        return tm.getUser();
    }

    @Override
    public void setUser(DemoiselleUser loggedUser) {
        tm.setUser(loggedUser);
    }

    @Override
    public DemoiselleUser getUser(String issuer, String audience) {
        return tm.getUser(issuer, audience);
    }

    @Override
    public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {
        tm.setUser(loggedUser, issuer, audience);
    }

    @Override
    public void removeUser(DemoiselleUser loggedUser) {
        tm.removeUser(loggedUser);
    }

}
