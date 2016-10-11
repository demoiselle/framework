package org.demoiselle.jee.security.interceptor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import static javax.ws.rs.Priorities.AUTHENTICATION;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import org.demoiselle.jee.security.annotation.LoggedIn;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@link LoggedIn} annotations.
 * </p>
 *
 * @author SERPRO
 */
@LoggedIn
@Interceptor
@Priority(AUTHENTICATION)
public class LoggedInInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoiselleSecurityMessages bundle;

    /**
     *
     * @param ic
     * @return
     * @throws Exception
     */
    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        if (!securityContext.isLoggedIn()) {
            throw new DemoiselleSecurityException(bundle.userNotAuthenticated(), UNAUTHORIZED.getStatusCode());
        }
        return ic.proceed();
    }
}
