/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import java.lang.reflect.Method;
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
@Priority(AUTHORIZATION)
public class CorsFilter implements ContainerResponseFilter {

    @Inject
    private DemoiselleSecurityConfig config;

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();
        boolean corsEnable = config.isCorsEnabled();

        res.getHeaders().putSingle("Demoiselle-security", "Enable");

        config.getParamsHeaderSecuriry().entrySet().stream().forEach((entry) -> {
            res.getHeaders().putSingle(entry.getKey(), entry.getValue());
        });

        if (method != null && classe != null && method.getAnnotation(Cors.class) != null) {
            corsEnable = method.getAnnotation(Cors.class).enable();
        }

        if (config.isCorsEnabled() && corsEnable) {
            config.getParamsHeaderCors().entrySet().stream().forEach((entry) -> {
                res.getHeaders().putSingle(entry.getKey(), entry.getValue());
            });
        } else {
            res.getHeaders().remove("Access-Control-Allow-Origin");
            res.getHeaders().remove("Access-Control-Allow-Methods");
        }

    }

}
