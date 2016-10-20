/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.annotation.Cors;

/**
 *
 * @author 70744416353
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    private DemoiselleSecurityConfig config;

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (req.getMethod().equals("OPTIONS")) {
            ResponseBuilder responseBuilder = Response.ok();
            if (config.isCorsEnabled()) {
                responseBuilder.header("Access-Control-Allow-Headers", "origin, content-type, accept, Authorization");
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
                responseBuilder.header("Access-Control-Allow-Origin", "*");
                responseBuilder.header("Access-Control-Allow-Methods", "HEAD, OPTIONS, TRACE, GET, POST, PUT, PATCH, DELETE");
                responseBuilder.header("Access-Control-Max-Age", "360000");
            }
            req.abortWith(responseBuilder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (config.isCorsEnabled()) {
            Method method = info.getResourceMethod();
            if (method != null) {
                Cors cors = method.getAnnotation(Cors.class);
                if (cors != null) {
                    responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", requestContext.getHeaders().getFirst("Origin"));
                    responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "Origin, Content-type, Accept, Authorization");
                    responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
                    responseContext.getHeaders().putSingle("Access-Control-Max-Age", "360000");
                    responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", requestContext.getMethod());
                }
            }
        }
    }
}
