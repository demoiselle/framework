package org.demoiselle.jee.security.basic.impl;

import javax.enterprise.context.Dependent;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.security.SecurityContext;
import org.demoiselle.jee.security.TokensManager;
import org.demoiselle.jee.security.exception.NotLoggedInException;

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

    private String token;

    private Principal user;

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
        return getUser() != null;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#getUser()
     */
    @Override
    public Principal getUser() {
        return this.user;
    }

    public void checkLoggedIn() throws NotLoggedInException {
        if (!isLoggedIn()) {
            throw new NotLoggedInException(bundle.getString("user-not-authenticated"));
        }
    }

    @Override
    public void setRoles(Set<String> roles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPermission(Map<String, String> permissions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getResources(String operation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getOperations(String resources) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setUser(Principal principal) {
        this.token = tm.create(principal);
        this.user = principal;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.user = tm.getUser(token);
        this.token = token;
    }


}
