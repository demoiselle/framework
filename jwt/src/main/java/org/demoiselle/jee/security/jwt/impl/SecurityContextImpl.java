package org.demoiselle.jee.security.jwt.impl;

import org.demoiselle.jee.security.Token;
import javax.enterprise.context.Dependent;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.security.interfaces.SecurityContext;
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

    @Inject
    private TokensManager tm;

    @Inject
    private Token token;

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
        if (token.getKey() != null && !token.getKey().isEmpty()) {
            return tm.getUser(token.getKey());
        }
        return token.getPrincipal();
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
        token.setKey(tm.getToken(principal));
        token.setPrincipal(principal);
    }

    @Override
    public String getToken() {
        if (token.getKey() != null && token.getKey().isEmpty()) {
            token.setKey(tm.getToken(token.getPrincipal()));
        }
        return token.getKey();
    }

    @Override
    public void setToken(String chave) {
        token.setPrincipal(tm.getUser(chave));
        if (token.getPrincipal() == null) {
            throw new NotLoggedInException(bundle.getString("user-not-authenticated"));
        }
        token.setKey(chave);
    }

}
