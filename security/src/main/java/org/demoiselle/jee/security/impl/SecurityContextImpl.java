package org.demoiselle.jee.security.impl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import org.demoiselle.jee.core.annotation.literal.NameQualifier;
import org.demoiselle.jee.core.annotation.literal.StrategyQualifier;
import org.demoiselle.jee.core.exception.DemoiselleException;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.security.AfterLoginSuccessful;
import org.demoiselle.jee.security.AfterLogoutSuccessful;
import org.demoiselle.jee.security.Authenticator;
import org.demoiselle.jee.security.Authorizer;
import org.demoiselle.jee.security.SecurityContext;
import org.demoiselle.jee.security.exception.AuthenticationException;
import org.demoiselle.jee.security.exception.AuthorizationException;
import org.demoiselle.jee.security.exception.NotLoggedInException;

/**
 * <p>
 * This is the default implementation of {@link SecurityContext} interface.
 * </p>
 *
 * @author SERPRO
 */
@Dependent
@Named("securityContext")
public class SecurityContextImpl implements SecurityContext {

    private static final long serialVersionUID = 1L;

    private transient ResourceBundle bundle;

    private Authenticator authenticator;

    private Authorizer authorizer;

    private Authenticator getAuthenticator() {
        if (this.authenticator == null) {
            Class<? extends Authenticator> type = getConfig().getAuthenticatorClass();

            if (type != null) {
                this.authenticator = CDI.current().select(type).get(); //Beans.getReference(type);
            } else {
                this.authenticator = CDI.current().select(Authenticator.class, new StrategyQualifier()).get(); // Beans.getReference(Authenticator.class, new StrategyQualifier());
            }
        }

        return this.authenticator;
    }

    private Authorizer getAuthorizer() {
        if (this.authorizer == null) {
            Class<? extends Authorizer> type = getConfig().getAuthorizerClass();

            if (type != null) {
                this.authorizer = CDI.current().select(type).get(); //Beans.getReference(type);
            } else {
                this.authorizer = CDI.current().select(Authorizer.class, new StrategyQualifier()).get(); //Beans.getReference(Authorizer.class, new StrategyQualifier());
            }
        }

        return this.authorizer;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#hasPermission(String,
     * String)
     */
    @Override
    public boolean hasPermission(String resource, String operation) {
        boolean result = true;

        if (getConfig().isEnabled()) {
            checkLoggedIn();

            try {
                result = getAuthorizer().hasPermission(resource, operation);

            } catch (DemoiselleException cause) {
                throw cause;

            } catch (Exception cause) {
                throw new AuthorizationException(cause);
            }
        }

        return result;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#hasRole(String)
     */
    @Override
    public boolean hasRole(String role) {
        boolean result = true;

        if (getConfig().isEnabled()) {
            checkLoggedIn();

            try {
                result = getAuthorizer().hasRole(role);

            } catch (DemoiselleException cause) {
                throw cause;

            } catch (Exception cause) {
                throw new AuthorizationException(cause);
            }
        }

        return result;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#isLoggedIn()
     */
    @Override
    public boolean isLoggedIn() {
        boolean result = true;

        if (getConfig().isEnabled()) {
            result = getUser() != null;
        }

        return result;
    }

    /**
     * @see org.demoiselle.security.SecurityContext#login()
     */
    @Override
    public void login() {
        if (getConfig().isEnabled()) {

            try {
                getAuthenticator().authenticate();

            } catch (DemoiselleException cause) {
                throw cause;

            } catch (Exception cause) {
                throw new AuthenticationException(cause);
            }

            CDI.current().getBeanManager().fireEvent(new AfterLoginSuccessful() {

                private static final long serialVersionUID = 1L;
            });
//			Beans.getBeanManager().fireEvent(new AfterLoginSuccessful() {
//
//				private static final long serialVersionUID = 1L;
//			});
        }
    }

    /**
     * @see org.demoiselle.security.SecurityContext#logout()
     */
    @Override
    public void logout() throws NotLoggedInException {
        if (getConfig().isEnabled()) {
            checkLoggedIn();

            try {
                getAuthenticator().unauthenticate();

            } catch (DemoiselleException cause) {
                throw cause;

            } catch (Exception cause) {
                throw new AuthenticationException(cause);
            }

            CDI.current().getBeanManager().fireEvent(new AfterLogoutSuccessful() {

                private static final long serialVersionUID = 1L;
            });
//			Beans.getBeanManager().fireEvent(new AfterLogoutSuccessful() {
//
//				private static final long serialVersionUID = 1L;
//			});
        }
    }

    /**
     * @see org.demoiselle.security.SecurityContext#getUser()
     */
    @Override
    public Principal getUser() {
        Principal user = getAuthenticator().getUser();

        if (!getConfig().isEnabled() && user == null) {
            user = new EmptyUser();
        }

        return user;
    }

    private SecurityConfig getConfig() {
        return CDI.current().select(SecurityConfig.class).get();
//		return Beans.getReference(SecurityConfig.class);
    }

    public void checkLoggedIn() throws NotLoggedInException {
        if (!isLoggedIn()) {
            throw new NotLoggedInException(getBundle().getString("user-not-authenticated"));
        }
    }

    private ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = CDI.current().select(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle")).get();
//			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
        }

        return bundle;
    }

    private static class EmptyUser implements Principal, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public String getName() {
            return "demoiselle";
        }
    }
}
