/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import static jakarta.ws.rs.Priorities.AUTHORIZATION;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.RequiredAnyRole;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@code @RequiredAnyRole} annotations.
 * Verifies that the user possesses at least one of the specified roles (OR logic).
 * </p>
 *
 * @author SERPRO
 */
@RequiredAnyRole(value = "")
@Interceptor
@Priority(AUTHORIZATION)
public class RequiredAnyRoleInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoiselleSecurityMessages bundle;

    /**
     * <p>
     * Gets the value property of {@code @RequiredAnyRole}. Delegates to
     * {@code SecurityContext} to check roles. If the user has at least one of
     * the required roles it executes the method, otherwise throws an exception.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return what is returned from the intercepted method.
     * @throws Exception if there is an error during the role check or during
     * the method's processing.
     */
    @AroundInvoke
    public Object manage(InvocationContext ic) throws Exception {
        if (!securityContext.isLoggedIn()) {
            throw new DemoiselleSecurityException(
                bundle.userNotAuthenticated(), UNAUTHORIZED.getStatusCode());
        }

        String[] roles = resolveRoles(ic);
        boolean hasAny = Arrays.stream(roles).anyMatch(securityContext::hasRole);

        if (!hasAny) {
            throw new DemoiselleSecurityException(
                bundle.doesNotHaveRole(Arrays.toString(roles)), FORBIDDEN.getStatusCode());
        }

        return ic.proceed();
    }

    /**
     * <p>
     * Resolves the roles from the {@code @RequiredAnyRole} annotation.
     * Method-level annotation takes precedence over class-level.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return the roles defined in the annotation.
     */
    private String[] resolveRoles(InvocationContext ic) {
        RequiredAnyRole ann = ic.getMethod().getAnnotation(RequiredAnyRole.class);
        if (ann == null) {
            ann = ic.getTarget().getClass().getAnnotation(RequiredAnyRole.class);
        }
        return ann.value();
    }
}
