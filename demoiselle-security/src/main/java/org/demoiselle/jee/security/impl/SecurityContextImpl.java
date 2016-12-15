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
 * TODO javadoc
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
    	//TODO tratar nullpointer
        return tm.getUser();
    }

    @Override
    public void setUser(DemoiselleUser loggedUser) {
        tm.setUser(loggedUser);
    }

    @Override
    public void removeUser(DemoiselleUser loggedUser) {
        tm.removeUser(loggedUser);
    }

}
