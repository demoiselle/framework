/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import static jakarta.ws.rs.core.Response.ok;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import static jakarta.ws.rs.Priorities.AUTHORIZATION;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.bruteforce.BruteForceGuard;
import org.demoiselle.jee.security.impl.TokenImpl;

/**
 * <p>
 * Server cors handling
 * </p>
 *
 * @see
 * <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/cors.html">Documentation</a>
 *
 * @author SERPRO
 */
@Provider
@PreMatching
@Priority(AUTHORIZATION)
@RequestScoped
public class SecurityFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(SecurityFilter.class.getName());

    @Inject
    private DemoiselleSecurityConfig config;

    @Inject
    private BruteForceGuard bruteForceGuard;

    @Inject
    private TokenManager tokenManager;

    @Inject
    private HttpServletRequest httpServletRequest;

    private TokenImpl currentToken;

    @Produces
    @RequestScoped
    @SuppressWarnings("deprecation")
    public Token produceToken() {
        if (currentToken != null) {
            return currentToken;
        }
        return new TokenImpl();
    }

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (req.getMethod().equals("OPTIONS")) {
            Response.ResponseBuilder responseBuilder = ok();
            if (config.isCorsEnabled()) {
                config.getParamsHeaderSecuriry().forEach((key, value) ->
                    responseBuilder.header(key, value));
                applyCorsHeaders(req, responseBuilder);
            }
            req.abortWith(responseBuilder.build());
            return;
        }

        String ip = httpServletRequest.getRemoteAddr();

        // Verifica bloqueio por brute force
        int retryAfter = bruteForceGuard.isBlocked(ip);
        if (retryAfter > 0) {
            req.abortWith(Response.status(429)
                .header("Retry-After", retryAfter)
                .entity("Too many failed attempts").build());
            return;
        }

        try {
            if (req.getHeaders().containsKey("Authorization")) {
                String chave = req.getHeaders().get("Authorization").toString()
                    .replace("[", "").replace("]", "");
                if (!chave.isEmpty()) {
                    TokenType tokenType = TokenType.valueOf(chave.split(" ")[0].toUpperCase());
                    String tokenKey = chave.split(" ")[1];
                    currentToken = new TokenImpl();
                    currentToken.setKey(tokenKey);
                    currentToken.setType(tokenType);

                    // Valida token e atualiza brute force guard
                    if (tokenManager.validate()) {
                        bruteForceGuard.resetAttempts(ip);
                    } else {
                        bruteForceGuard.recordFailedAttempt(ip);
                    }
                }
            }
        } catch (Exception e) {
            bruteForceGuard.recordFailedAttempt(ip);
            logger.severe(e.getMessage());
        }

        if (currentToken == null) {
            currentToken = new TokenImpl();
        }
    }

    private void applyCorsHeaders(ContainerRequestContext req,
                                  Response.ResponseBuilder responseBuilder) {
        List<String> allowedOrigins = config.getCorsAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            config.getParamsHeaderCors().forEach((key, value) ->
                responseBuilder.header(key, value));
            return;
        }

        String origin = req.getHeaderString("Origin");
        String resolvedOrigin = resolveAllowedOrigin(origin, allowedOrigins,
                config.isCorsAllowCredentials());
        if (origin != null && resolvedOrigin == null) {
            return;
        }

        if (resolvedOrigin != null) {
            responseBuilder.header("Access-Control-Allow-Origin", resolvedOrigin);
            if (!"*".equals(resolvedOrigin) && origin != null) {
                responseBuilder.header("Vary", "Origin");
            }
            if (config.isCorsAllowCredentials() && !"*".equals(resolvedOrigin)) {
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
            }
        }

        if (!config.getCorsAllowedMethods().isEmpty()) {
            responseBuilder.header("Access-Control-Allow-Methods",
                    String.join(", ", config.getCorsAllowedMethods()));
        }

        if (!config.getCorsAllowedHeaders().isEmpty()) {
            responseBuilder.header("Access-Control-Allow-Headers",
                    String.join(", ", config.getCorsAllowedHeaders()));
        }

        responseBuilder.header("Access-Control-Max-Age",
                String.valueOf(config.getCorsMaxAge()));
    }

    private String resolveAllowedOrigin(String origin, List<String> allowedOrigins,
                                        boolean allowCredentials) {
        if (allowedOrigins.contains("*")) {
            if (allowCredentials && origin != null) {
                return origin;
            }
            return "*";
        }
        if (origin != null && allowedOrigins.contains(origin)) {
            return origin;
        }
        return null;
    }
}
