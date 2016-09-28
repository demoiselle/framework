/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.demoiselle.jee.core.annotation.Name;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.core.util.Strings;
import org.demoiselle.jee.security.annotation.RequiredPermission;
import org.demoiselle.jee.core.interfaces.security.SecurityContext;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@code @RequiredPermission} annotation.
 * </p>
 *
 * @author SERPRO
 */
@RequiredPermission
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class RequiredPermissionInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoisellePrincipal loggedUser;

    @Inject
    private DemoiselleSecurityMessages bundle;

    @Inject
    private Logger logger;

    /**
     * <p>
     * Gets the values for both resource and operation properties of
     * {@code @RequiredPermission}. Delegates to {@code SecurityContext} check
     * permissions. If the user has the required permission it executes the
     * mehtod, otherwise throws an exception. Returns what is returned from the
     * intercepted method. If the method's return type is {@code void} returns
     * {@code null}.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return what is returned from the intercepted method. If the method's
     * return type is {@code void} returns {@code null}.
     * @throws Exception if there is an error during the permission check or
     * during the method's processing.
     */
    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        String resource = getResource(ic);
        String operation = getOperation(ic);
        String username = null;

        if (securityContext.isLoggedIn()) {
            username = loggedUser.getName();
            logger.finest(bundle.accessCheckingPermission(username, operation, resource));
        }

        if (securityContext.hasPermission(resource, operation)) {
            logger.severe(bundle.doesNotHavePermission(username, operation, resource));
            throw new DemoiselleSecurityException(bundle.doesNotHavePermission(username, operation, resource), Response.Status.UNAUTHORIZED.getStatusCode());
        }

        logger.fine(bundle.accessAllowed(username, operation, resource));
        return ic.proceed();
    }

    /**
     * <p>
     * Returns the resource defined in {@code @RequiredPermission} annotation,
     * the name defined in {@code @AmbiguousQualifier} annotation or the class
     * name itself.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return the resource defined in {@code @RequiredPermission} annotation,
     * the name defined in {@code @AmbiguousQualifier} annotation or the class
     * name itself.
     */
    private String getResource(InvocationContext ic) {
        RequiredPermission requiredPermission;
        requiredPermission = ic.getMethod().getAnnotation(RequiredPermission.class);

        if (requiredPermission == null) {
            requiredPermission = ic.getTarget().getClass().getAnnotation(RequiredPermission.class);
        }

        if (Strings.isEmpty(requiredPermission.resource())) {
            if (ic.getTarget().getClass().getAnnotation(Name.class) == null) {
                return ic.getTarget().getClass().getSimpleName();
            } else {
                return ic.getTarget().getClass().getAnnotation(Name.class).value();
            }
        } else {
            return requiredPermission.resource();
        }
    }

    /**
     * <p>
     * Returns the operation defined in {@code @RequiredPermission} annotation,
     * the name defined in {@code @AmbiguousQualifier} annotation or the
     * method's name itself.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return the operation defined in {@code @RequiredPermission} annotation,
     * the name defined in {@code @AmbiguousQualifier} annotation or the
     * method's name itself.
     */
    private String getOperation(InvocationContext ic) {
        RequiredPermission requiredPermission;
        requiredPermission = ic.getMethod().getAnnotation(RequiredPermission.class);

        if (requiredPermission == null) {
            requiredPermission = ic.getTarget().getClass().getAnnotation(RequiredPermission.class);
        }

        if (Strings.isEmpty(requiredPermission.operation())) {
            if (ic.getMethod().getAnnotation(Name.class) == null) {
                return ic.getMethod().getName();
            } else {
                return ic.getMethod().getAnnotation(Name.class).value();
            }
        } else {
            return requiredPermission.operation();
        }
    }

}
