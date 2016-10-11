/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
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
import org.demoiselle.jee.core.api.security.Token;

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
    private Token token;

    /**
     *
     */
    @PostConstruct
    public void init() {
        LOG.info("Demoiselle Module - Security");
    }

    /**
     *
     * @param requestContext
     * @param responseContext
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "Authorization");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");

        responseContext.getHeaders().putSingle("Authorization", "enabled");
        responseContext.getHeaders().putSingle("x-content-type-options", "nosniff");
        responseContext.getHeaders().putSingle("x-frame-options", "SAMEORIGIN");
        responseContext.getHeaders().putSingle("x-xss-protection", "1; mode=block");
    }

    /**
     *
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            if (requestContext.getHeaders().containsKey("Authorization")) {
                String chave = requestContext.getHeaders().get("Authorization").toString().replace("[", "").replace("]", "");
                if (!chave.isEmpty()) {
                    token.setType(chave.split(" ")[0]);
                    token.setKey(chave.split(" ")[1]);
                }
            }
        } catch (Exception e) {
            LOG.fine(e.getMessage());
        }

    }

    /**
     *
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {

    }

    /**
     *
     * @param requestContext
     * @param responseContext
     * @throws IOException
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

    }

}
