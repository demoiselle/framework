/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import static javax.ws.rs.Priorities.AUTHENTICATION;
import static javax.ws.rs.core.Response.ok;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;

/**
 *
 * @author SERPRO
 */
@Provider
@PreMatching
@Priority(AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    private Logger logger;

    @Inject
    private DemoiselleSecurityConfig config;

    @Inject
    private Token token;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (req.getMethod().equals("OPTIONS")) {
            Response.ResponseBuilder responseBuilder = ok();
            if (config.isCorsEnabled()) {
            	//TODO deixar parametrizado no demoiselle.properties
                responseBuilder.header("Access-Control-Allow-Headers", "origin, content-type, accept, Authorization");
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
                responseBuilder.header("Access-Control-Allow-Origin", "*");
                responseBuilder.header("Access-Control-Allow-Methods", "HEAD, OPTIONS, TRACE, GET, POST, PUT, PATCH, DELETE");
            }
            //TODO deixar parametrizado no demoiselle.properties
            responseBuilder.header("Access-Control-Max-Age", "3600000");
            req.abortWith(responseBuilder.build());
        }

        try {
            if (req.getHeaders().containsKey("Authorization")) {
                String chave = req.getHeaders().get("Authorization").toString().replace("[", "").replace("]", "");
                if (!chave.isEmpty()) {
                    token.setType(chave.split(" ")[0]);
                    token.setKey(chave.split(" ")[1]);
                }
            }
        } catch (Exception e) {
        	//TODO usar mensagem do demoiselle
            logger.severe(e.getMessage());
        }
    }
}
