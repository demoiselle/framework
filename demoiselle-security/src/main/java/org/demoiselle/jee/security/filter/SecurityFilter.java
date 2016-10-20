/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.rest.annotation.CacheControl;
import org.demoiselle.jee.rest.annotation.Cors;

/**
 *
 * @author 70744416353
 */
@Provider
@PreMatching
public class SecurityFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Inject
    private Logger logger;

    @Inject
    private Token token;

    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        res.getHeaders().putSingle("Authorization", "enabled");
        res.getHeaders().putSingle("x-content-type-options", "nosniff");
        res.getHeaders().putSingle("x-frame-options", "SAMEORIGIN");
        res.getHeaders().putSingle("x-xss-protection", "1; mode=block");
    }

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
            logger.fine(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Demoiselle Module - Security");
    }
}
