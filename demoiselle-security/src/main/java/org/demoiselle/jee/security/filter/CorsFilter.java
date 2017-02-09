/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Priority;

import javax.inject.Inject;
import static javax.ws.rs.Priorities.AUTHORIZATION;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

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

        config.getParamsHeaderSecuriry().entrySet().parallelStream().forEach((entry) -> {
            res.getHeaders().putSingle(entry.getKey(), entry.getValue());
        });

        if (method != null && classe != null && method.getAnnotation(Cors.class) != null) {
            corsEnable = method.getAnnotation(Cors.class).enable();
        }

        if (config.isCorsEnabled() && corsEnable) {
            config.getParamsHeaderCors().entrySet().parallelStream().forEach((entry) -> {
                res.getHeaders().putSingle(entry.getKey(), entry.getValue());
            });
        } else {
            res.getHeaders().remove("Access-Control-Allow-Origin");
            res.getHeaders().remove("Access-Control-Allow-Methods");
        }

    }

}
