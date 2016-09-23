/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security;

import java.io.IOException;
import org.demoiselle.jee.security.interfaces.SecurityContext;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author 70744416353
 */
@Provider
@PreMatching
public class JaxRsFilter implements ClientRequestFilter, ClientResponseFilter, ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    private Logger LOG;

    @Inject
    private SecurityContext securityContext;

    @PostConstruct
    public void init() {
        LOG.info("Demoiselle Module - Security");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Authorization", "enabled");
        responseContext.getHeaders().putSingle("x-content-type-options", "nosniff");
        responseContext.getHeaders().putSingle("x-frame-options", "SAMEORIGIN");
        responseContext.getHeaders().putSingle("x-xss-protection", "1; mode=block");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            if (requestContext.getHeaders().containsKey("Authorization")) {
                String token = requestContext.getHeaders().get("Authorization").toString().replace("[", "").replace("]", "");
                if (!token.isEmpty()) {
                    securityContext.setToken(token);
                }
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {

    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

    }

}
