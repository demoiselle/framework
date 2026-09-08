/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import static jakarta.ws.rs.Priorities.HEADER_DECORATOR;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.rest.DemoiselleRestConfig;

/**
 *
 * @author SERPRO
 *
 */
@Provider
@Priority(HEADER_DECORATOR)
public class RestFilter implements ContainerResponseFilter {

    @Inject
    private DemoiselleMessage demoiselleMessage;

    @Inject
    private DemoiselleRestConfig config;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (config != null && config.isExposeFrameworkVersion()
                && !responseContext.getHeaders().containsKey("Demoiselle-Version")) {
            responseContext.getHeaders().putSingle("Demoiselle-Version", demoiselleMessage.frameworkName());
        }

        if (config != null && config.isSecurityHeadersEnabled()) {
            Map<String, String> securityHeaders = config.getSecurityHeaders();
            if (securityHeaders != null) {
                securityHeaders.forEach((name, value) -> {
                    if (name != null && value != null && !responseContext.getHeaders().containsKey(name)) {
                        responseContext.getHeaders().putSingle(name, value);
                    }
                });
            }
        }
    }
}
