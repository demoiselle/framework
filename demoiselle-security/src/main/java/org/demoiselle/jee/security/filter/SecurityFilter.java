/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.AUTHENTICATION;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.ok;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;

/**
 *
 * @author 70744416353
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
                responseBuilder.header("Access-Control-Allow-Headers", "origin, content-type, accept, Authorization");
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
                responseBuilder.header("Access-Control-Allow-Origin", "*");
                responseBuilder.header("Access-Control-Allow-Methods", "HEAD, OPTIONS, TRACE, GET, POST, PUT, PATCH, DELETE");
            }
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
            logger.fine(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Demoiselle Module: Security");
        logger.log(INFO, "CORS Enabled :{0}", config.isCorsEnabled());
    }
}
