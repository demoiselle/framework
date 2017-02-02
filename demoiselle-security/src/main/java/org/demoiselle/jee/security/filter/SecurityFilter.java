/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import static javax.ws.rs.core.Response.ok;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.AUTHORIZATION;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;

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
public class SecurityFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(SecurityFilter.class.getName());

    @Inject
    private DemoiselleSecurityConfig config;

    @Inject
    private Token token;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (req.getMethod().equals("OPTIONS")) {
            Response.ResponseBuilder responseBuilder = ok();
            if (config.isCorsEnabled()) {
                config.getParamsHeaderSecuriry().entrySet().parallelStream().forEach((entry) -> {
                    responseBuilder.header(entry.getKey(), entry.getValue());
                });
            }
            req.abortWith(responseBuilder.build());
        }

        try {
            if (req.getHeaders().containsKey("Authorization")) {
                String chave = req.getHeaders().get("Authorization").toString().replace("[", "").replace("]", "");
                if (!chave.isEmpty()) {
                    token.setType(TokenType.valueOf(chave.split(" ")[0].toUpperCase()));
                    token.setKey(chave.split(" ")[1]);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}
