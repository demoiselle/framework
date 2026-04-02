/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import static jakarta.ws.rs.Priorities.AUTHORIZATION;

import java.io.Serializable;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.treatment.ProblemDetail;
import org.demoiselle.jee.security.annotation.RateLimit;
import org.demoiselle.jee.security.ratelimit.SlidingWindowCounter;

/**
 * <p>
 * Intercepts calls with {@code @RateLimit} annotations.
 * Extracts the client IP from the {@code HttpServletRequest} and delegates
 * to {@code SlidingWindowCounter} to enforce rate limits.
 * Rejects requests exceeding the limit with HTTP 429 (Too Many Requests)
 * and a {@code Retry-After} header, returning a JAX-RS {@code Response}
 * directly instead of throwing an exception.
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

    @Inject
    private Instance<DemoiselleRestConfig> restConfig;

    /**
     * <p>
     * Intercepts the method invocation, extracts the client IP, and checks
     * the rate limit via {@code SlidingWindowCounter}. If the limit is exceeded,
     * returns a JAX-RS {@code Response} with status 429 and {@code Retry-After}
     * header directly, aborting the invocation context.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being called.
     * @return what is returned from the intercepted method, or a 429 Response if rate limited.
     * @throws Exception if there is an error during the method's processing.
     */
    @AroundInvoke
    public Object manage(InvocationContext ic) throws Exception {
        RateLimit rateLimit = resolveAnnotation(ic);
        String clientKey = request.getRemoteAddr();

        int retryAfter = counter.recordAndCheck(clientKey, rateLimit.requests(), rateLimit.window());
        if (retryAfter > 0) {
            return buildRateLimitResponse(retryAfter);
        }

        return ic.proceed();
    }

    /**
     * Builds a 429 Too Many Requests response with the Retry-After header.
     * When the REST module is available and RFC 9457 format is active, returns
     * an {@code application/problem+json} body. Otherwise returns a simple
     * JSON body with an {@code error} field.
     *
     * @param retryAfter seconds until the client may retry
     * @return a JAX-RS Response with status 429
     */
    Response buildRateLimitResponse(int retryAfter) {
        boolean useRfc9457 = restConfig.isResolvable() && restConfig.get().isRfc9457();

        Object body;
        String mediaType;
        if (useRfc9457) {
            ProblemDetail pd = new ProblemDetail();
            pd.setType("urn:demoiselle:rate-limit-exceeded");
            pd.setTitle("Too Many Requests");
            pd.setStatus(429);
            pd.setDetail("Retry after " + retryAfter + " seconds");
            body = pd;
            mediaType = "application/problem+json";
        } else {
            body = Map.of("error", "Rate limit exceeded. Retry after " + retryAfter + " seconds");
            mediaType = "application/json";
        }

        return Response.status(429)
            .header("Retry-After", retryAfter)
            .entity(body)
            .type(mediaType)
            .build();
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
