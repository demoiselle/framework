/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import java.io.Serializable;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.Authenticated;
import org.demoiselle.jee.security.event.AuthenticationEvent;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@link Authenticated} annotations.
 * </p>
 *
 * @author SERPRO
 */
@Authenticated
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AuthenticatedInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoiselleSecurityMessages bundle;

    @Inject
    private Event<AuthenticationEvent> authEvent;

    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        Authenticated logged = ic.getMethod().getAnnotation(Authenticated.class);

        if (logged != null && !logged.enable()) {
            return ic.proceed();
        }

        if (!securityContext.isLoggedIn()) {
            authEvent.fire(new AuthenticationEvent(null, AuthenticationEvent.Action.FAILURE));
            throw new DemoiselleSecurityException(bundle.userNotAuthenticated(), FORBIDDEN.getStatusCode());
        }

        return ic.proceed();
    }
}
