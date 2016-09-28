/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;

import org.demoiselle.jee.core.interfaces.security.SecurityContext;
import org.demoiselle.jee.core.interfaces.security.TokensManager;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * This is the default implementation of {@link SecurityContext} interface.
 * </p>
 *
 * @author SERPRO
 */
@Dependent
public class SecurityContextImpl implements SecurityContext {

    private static final long serialVersionUID = 1L;

    @Inject
    private TokensManager tm;

    /**
     * @see org.demoiselle.security.SecurityContext#hasPermission(String,
     * String)
     */
    @Override
    public boolean hasPermission(String resource, String operation) {
        if ((tm.getUser().getPermissions().entrySet()
                .stream()
                .filter(p -> p.getKey().equalsIgnoreCase(resource))
                .filter(p -> p.getValue().equalsIgnoreCase(operation))
                .count() <= 0)) {
            return false;
        }
        return true;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#hasRole(String)
     */
    @Override
    public boolean hasRole(String role) {
        if (tm.getUser().getRoles().parallelStream().filter(p -> p.equals(role)).count() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#isLoggedIn()
     */
    @Override
    public boolean isLoggedIn() {
        return getUser() != null;
    }

    @Override
    public DemoisellePrincipal getUser() {
        return tm.getUser();
    }

    @Override
    public void setUser(DemoisellePrincipal loggedUser) {
        tm.setUser(loggedUser);
    }

}
