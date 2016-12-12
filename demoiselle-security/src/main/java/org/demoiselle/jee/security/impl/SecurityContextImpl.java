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
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.TokenManager;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class SecurityContextImpl implements SecurityContext {

    private static final long serialVersionUID = 1L;

    @Inject
    private TokenManager tm;

    /**
     *
     * @param resource
     * @param operation
     * @return
     */
    @Override
    public boolean hasPermission(String resource, String operation) {

        List<String> lista = tm.getUser().getPermissions().get(resource);

        if (lista != null && !lista.isEmpty()) {
            return lista.contains(operation);
        }

        return false;
    }

    /**
     *
     * @param role
     * @return
     */
    @Override
    public boolean hasRole(String role) {
        return tm.getUser().getRoles().contains(role);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isLoggedIn() {
        return tm.validate();
    }

    /**
     *
     * @return
     */
    @Override
    public DemoisellePrincipal getUser() {
        return tm.getUser();
    }

    /**
     *
     * @param loggedUser
     */
    @Override
    public void setUser(DemoisellePrincipal loggedUser) {
        tm.setUser(loggedUser);
    }

    /**
     *
     * @param loggedUser
     */
    @Override
    public void removeUser(DemoisellePrincipal loggedUser) {
        tm.removeUser(loggedUser);
    }

}
