/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;

import org.demoiselle.jee.security.exception.NotLoggedInException;
import org.demoiselle.jee.core.interfaces.security.SecurityContext;
import org.demoiselle.jee.core.interfaces.security.TokensManager;
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

    @Inject
    private DemoiselleSecurityMessages bundle;

    /**
     * @see org.demoiselle.security.SecurityContext#hasPermission(String,
     * String)
     */
    @Override
    public boolean hasPermission(String resource, String operation) {
        return (tm.getUser().getPermissions().entrySet()
                .stream()
                .filter(p -> p.getKey().equalsIgnoreCase(resource))
                .filter(p -> p.getValue().equalsIgnoreCase(operation))
                .count() > 0);
    }

    /**
     * @see org.demoiselle.security.SecurityContext#hasRole(String)
     */
    @Override
    public boolean hasRole(String role) {
        return (tm.getUser().getRoles().parallelStream().filter(p -> p.equals(role)).count() > 0);
    }

    /**
     * @see org.demoiselle.security.SecurityContext#isLoggedIn()
     */
    @Override
    public boolean isLoggedIn() {
        return tm.validate();
    }

    @Override
    public void checkLoggedIn() throws NotLoggedInException {
        if (!isLoggedIn()) {
            throw new NotLoggedInException(bundle.userNotAuthenticated());
        }
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
