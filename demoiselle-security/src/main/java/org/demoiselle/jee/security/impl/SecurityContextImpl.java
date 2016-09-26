/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.demoiselle.jee.core.util.ResourceBundle;

import org.demoiselle.jee.security.exception.NotLoggedInException;
import org.demoiselle.jee.core.interfaces.security.SecurityContext;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;

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
    private ResourceBundle bundle;

    /**
     * @see org.demoiselle.security.SecurityContext#hasPermission(String,
     * String)
     */
    @Override
    public boolean hasPermission(String resource, String operation) {
        boolean result = true;

        return result;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#hasRole(String)
     */
    @Override
    public boolean hasRole(String role) {
        boolean result = true;

        return result;
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
            throw new NotLoggedInException(bundle.getString("user-not-authenticated"));
        }
    }

    @Override
    public Principal getUser() {
        return tm.getUser();
    }

    @Override
    public void setUser(Principal loggedUser) {
        tm.setUser(loggedUser);
    }

}
