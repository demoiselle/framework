/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import static jakarta.ws.rs.Priorities.AUTHORIZATION;

import java.io.Serializable;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.http.HttpServletRequest;

import org.demoiselle.jee.security.annotation.RateLimit;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.ratelimit.SlidingWindowCounter;

/**
 * <p>
 * Intercepts calls with {@code @RateLimit} annotations.
 * Extracts the client IP from the {@code HttpServletRequest} and delegates
 * to {@code SlidingWindowCounter} to enforce rate limits.
 * Rejects requests exceeding the limit with HTTP 429 (Too Many Requests).
 * </p>
 * <p>
 * Priority is set to {@code AUTHORIZATION - 100} so it executes before
 * authorization interceptors.
 * </p>
 *
 * @author SERPRO
 */
@RateLimit
@Interceptor
@Priority(AUTHORIZATION - 100)
public class RateLimitInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SlidingWindowCounter counter;

    @Inject
    private HttpServletRequest request;

    /**
     * <p>
     * Intercepts the method invocation, extracts the client IP, and checks
     * the rate limit via {@code SlidingWindowCounter}. If the limit is exceeded,
     * throws a {@code DemoiselleSecurityException} with status 429.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return what is returned from the intercepted method.
     * @throws Exception if the rate limit is exceeded or if there is an error
     *         during the method's processing.
     */
    @AroundInvoke
    public Object manage(InvocationContext ic) throws Exception {
        RateLimit rateLimit = resolveAnnotation(ic);
        String clientKey = request.getRemoteAddr();

        int retryAfter = counter.recordAndCheck(clientKey, rateLimit.requests(), rateLimit.window());
        if (retryAfter > 0) {
            throw new DemoiselleSecurityException(
                "Rate limit exceeded. Retry after " + retryAfter + " seconds",
                429
            );
        }

        return ic.proceed();
    }

    /**
     * <p>
     * Resolves the {@code @RateLimit} annotation from the invocation context.
     * Method-level annotation takes precedence over class-level.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return the {@code @RateLimit} annotation found.
     */
    private RateLimit resolveAnnotation(InvocationContext ic) {
        RateLimit methodLevel = ic.getMethod().getAnnotation(RateLimit.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return ic.getTarget().getClass().getAnnotation(RateLimit.class);
    }
}
