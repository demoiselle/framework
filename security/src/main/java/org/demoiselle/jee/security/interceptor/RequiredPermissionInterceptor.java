package org.demoiselle.jee.security.interceptor;

import org.demoiselle.jee.security.exception.AuthorizationException;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.inject.Inject;
import static javax.sql.rowset.spi.SyncFactory.getLogger;
import org.demoiselle.jee.core.annotation.Name;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.core.util.Strings;
import org.demoiselle.jee.security.RequiredPermission;
import org.demoiselle.jee.security.SecurityContext;

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
    private ResourceBundle bundle;

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

        if (getSecurityContext().isLoggedIn()) {
            username = getSecurityContext().getUser().getName();
            getLogger().finest(bundle.getString("access-checking", username, operation, resource));
        }

        if (!getSecurityContext().hasPermission(resource, operation)) {
            getLogger().severe(bundle.getString("access-denied", username, operation, resource));
            throw new AuthorizationException(bundle.getString("access-denied-ui", resource, operation));
        }

        getLogger().fine(bundle.getString("access-allowed", username, operation, resource));
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

    private SecurityContext getSecurityContext() {
        return CDI.current().select(SecurityContext.class).get();
    }
}
