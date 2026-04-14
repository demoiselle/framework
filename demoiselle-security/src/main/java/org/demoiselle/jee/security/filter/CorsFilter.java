/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import static jakarta.ws.rs.Priorities.AUTHORIZATION;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.annotation.Cors;

/**
 * JAX-RS response filter that applies CORS and security headers.
 *
 * @author SERPRO
 */
@Provider
@Priority(AUTHORIZATION)
public class CorsFilter implements ContainerResponseFilter {

    @Inject
    private DemoiselleSecurityConfig config;

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        // Security headers (always applied)
        res.getHeaders().putSingle("Demoiselle-security", "Enable");
        config.getParamsHeaderSecuriry().forEach((key, value) ->
            res.getHeaders().putSingle(key, value));

        // Check if CORS is enabled (@Cors annotation takes precedence)
        boolean corsEnable = config.isCorsEnabled();
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();
        if (method != null && classe != null && method.getAnnotation(Cors.class) != null) {
            corsEnable = method.getAnnotation(Cors.class).enable();
        }

        if (!corsEnable) {
            removeCorsHeaders(res);
            return;
        }

        List<String> allowedOrigins = config.getCorsAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            config.getParamsHeaderCors().forEach((key, value) ->
                res.getHeaders().putSingle(key, value));
            return;
        }

        String origin = req.getHeaderString("Origin");
        String resolvedOrigin = resolveAllowedOrigin(origin, allowedOrigins,
                config.isCorsAllowCredentials());
        if (origin != null && resolvedOrigin == null) {
            return;
        }

        applyCorsHeaders(res, origin, resolvedOrigin);
    }

    private void applyCorsHeaders(ContainerResponseContext res, String origin,
                                  String resolvedOrigin) {
        if (resolvedOrigin != null) {
            res.getHeaders().putSingle("Access-Control-Allow-Origin", resolvedOrigin);
            if (!"*".equals(resolvedOrigin) && origin != null) {
                appendVaryOrigin(res);
            }
            if (config.isCorsAllowCredentials() && !"*".equals(resolvedOrigin)) {
                res.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            }
        }

        if (!config.getCorsAllowedMethods().isEmpty()) {
            res.getHeaders().putSingle("Access-Control-Allow-Methods",
                    String.join(", ", config.getCorsAllowedMethods()));
        }

        if (!config.getCorsAllowedHeaders().isEmpty()) {
            res.getHeaders().putSingle("Access-Control-Allow-Headers",
                    String.join(", ", config.getCorsAllowedHeaders()));
        }

        res.getHeaders().putSingle("Access-Control-Max-Age",
                String.valueOf(config.getCorsMaxAge()));
    }

    private void appendVaryOrigin(ContainerResponseContext res) {
        Object current = res.getHeaders().getFirst("Vary");
        if (current == null) {
            res.getHeaders().putSingle("Vary", "Origin");
            return;
        }
        String currentValue = current.toString();
        for (String item : currentValue.split(",")) {
            if ("Origin".equalsIgnoreCase(item.trim())) {
                return;
            }
        }
        res.getHeaders().putSingle("Vary", currentValue + ", Origin");
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

    private void removeCorsHeaders(ContainerResponseContext res) {
        res.getHeaders().remove("Access-Control-Allow-Origin");
        res.getHeaders().remove("Access-Control-Allow-Methods");
        res.getHeaders().remove("Access-Control-Allow-Headers");
        res.getHeaders().remove("Access-Control-Max-Age");
        res.getHeaders().remove("Access-Control-Allow-Credentials");
    }
}
