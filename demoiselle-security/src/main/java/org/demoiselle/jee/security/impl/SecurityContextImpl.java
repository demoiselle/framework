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

    @Inject
    private TokenManager tm;

    @Override
    public boolean hasPermission(String resource, String operation) {

        List<String> lista = tm.getUser().getPermissions().get(resource);

        if (lista != null && !lista.isEmpty()) {
            return lista.contains(operation);
        }

        return false;
    }

    @Override
    public boolean hasRole(String role) {
        return tm.getUser().getRoles().contains(role);
    }

    @Override
    public boolean isLoggedIn() {
        return tm.validate();
    }

    @Override
    public DemoisellePrincipal getUser() {
        return tm.getUser();
    }

    @Override
    public void setUser(DemoisellePrincipal loggedUser) {
        tm.setUser(loggedUser);
    }

    @Override
    public void removeUser(DemoisellePrincipal loggedUser) {
        tm.removeUser(loggedUser);
    }

}
