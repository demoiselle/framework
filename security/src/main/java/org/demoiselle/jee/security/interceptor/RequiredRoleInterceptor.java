package org.demoiselle.jee.security.interceptor;

import org.demoiselle.jee.security.exception.AuthorizationException;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Logger;
import javax.inject.Inject;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.security.annotations.RequiredRole;
import org.demoiselle.jee.security.interfaces.SecurityContext;

/**
 * <p>
 * Intercepts calls with {@code @RequiredRole} annotations.
 * </p>
 *
 * @author SERPRO
 */
@RequiredRole(value = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class RequiredRoleInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ResourceBundle bundle;

    @Inject
    private Logger logger;

    /**
     * <p>
     * Gets the value property of {@code @RequiredRole}. Delegates to
     * {@code SecurityContext} check role. If the user has the required role it
     * executes the mehtod, otherwise throws an exception. Returns what is
     * returned from the intercepted method. If the method's return type is
     * {@code void} returns {@code null}.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return what is returned from the intercepted method. If the method's
     * return type is {@code void} returns {@code null}.
     * @throws Exception if there is an error during the role check or during
     * the method's processing.
     */
    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        List<String> roles = getRoles(ic);

        if (getSecurityContext().isLoggedIn()) {
            logger.info(
                    bundle.getString("has-role-verification", getSecurityContext().getUser().getName(), roles));
        }

        List<String> userRoles = new ArrayList<String>();

        for (String role : roles) {
            if (getSecurityContext().hasRole(role)) {
                userRoles.add(role);
            }
        }

        if (userRoles.isEmpty()) {
            logger.severe(
                    bundle.getString("does-not-have-role", getSecurityContext().getUser().getName(), roles));

            throw new AuthorizationException(bundle.getString("does-not-have-role-ui", roles));
        }

        logger.fine(bundle.getString("user-has-role", getSecurityContext().getUser().getName(), userRoles));

        return ic.proceed();
    }

    /**
     * <p>
     * Returns the value defined in {@code @RequiredRole} annotation.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return the value defined in {@code @RequiredRole} annotation.
     */
    private List<String> getRoles(InvocationContext ic) {
        String[] roles = {};

        if (ic.getMethod().getAnnotation(RequiredRole.class) == null) {
            roles = ic.getTarget().getClass().getAnnotation(RequiredRole.class).value();
        } else {
            roles = ic.getMethod().getAnnotation(RequiredRole.class).value();
        }

        return Arrays.asList(roles);
    }

    private SecurityContext getSecurityContext() {
        return CDI.current().select(SecurityContext.class).get();
    }

}
