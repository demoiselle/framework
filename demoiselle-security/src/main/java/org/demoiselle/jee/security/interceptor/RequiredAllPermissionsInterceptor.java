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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.Permission;
import org.demoiselle.jee.security.annotation.RequiredAllPermissions;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@code @RequiredAllPermissions} annotations.
 * Verifies that the user possesses all of the specified permissions (AND logic).
 * </p>
 *
 * @author SERPRO
 */
@RequiredAllPermissions(value = {})
@Interceptor
@Priority(AUTHORIZATION)
public class RequiredAllPermissionsInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoiselleSecurityMessages bundle;

    /**
     * <p>
     * Gets the value property of {@code @RequiredAllPermissions}. Delegates to
     * {@code SecurityContext} to check permissions. If the user has all of
     * the required permissions it executes the method, otherwise throws an exception.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return what is returned from the intercepted method.
     * @throws Exception if there is an error during the permission check or during
     * the method's processing.
     */
    @AroundInvoke
    public Object manage(InvocationContext ic) throws Exception {
        if (!securityContext.isLoggedIn()) {
            throw new DemoiselleSecurityException(
                bundle.userNotAuthenticated(), UNAUTHORIZED.getStatusCode());
        }

        Permission[] permissions = resolvePermissions(ic);
        for (Permission perm : permissions) {
            if (!securityContext.hasPermission(perm.resource(), perm.operation())) {
                throw new DemoiselleSecurityException(
                    bundle.doesNotHavePermission(perm.operation(), perm.resource()),
                    FORBIDDEN.getStatusCode());
            }
        }

        return ic.proceed();
    }

    /**
     * <p>
     * Resolves the permissions from the {@code @RequiredAllPermissions} annotation.
     * Method-level annotation takes precedence over class-level.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return the permissions defined in the annotation.
     */
    private Permission[] resolvePermissions(InvocationContext ic) {
        RequiredAllPermissions ann = ic.getMethod().getAnnotation(RequiredAllPermissions.class);
        if (ann == null) {
            ann = ic.getTarget().getClass().getAnnotation(RequiredAllPermissions.class);
        }
        return ann.value();
    }
}
