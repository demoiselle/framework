/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import org.demoiselle.jee.security.annotations.LoggedIn;
import org.demoiselle.jee.security.interfaces.SecurityContext;

/**
 * <p>
 * Intercepts calls with {@link LoggedIn} annotations.
 * </p>
 *
 * @author SERPRO
 */
@LoggedIn
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggedInInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        securityContext.checkLoggedIn();
        return ic.proceed();
    }
}
