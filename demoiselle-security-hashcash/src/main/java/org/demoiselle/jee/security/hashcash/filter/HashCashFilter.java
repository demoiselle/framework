/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.HEADER_DECORATOR;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.security.hashcash.annotation.HashCash;
import org.demoiselle.jee.security.hashcash.execution.Generator;

/**
 *
 * @author SERPRO
 */
@Provider
@Priority(HEADER_DECORATOR)
public class HashCashFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(HashCashFilter.class.getName());

    @Context
    private ResourceInfo info;

    @Inject
    private Generator gera;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        //throw new DemoiselleRestException("Não implementado", Response.Status.NOT_IMPLEMENTED.getStatusCode());
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method != null && classe != null && method.getAnnotation(HashCash.class) != null) {
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.PARTIAL_CONTENT);
            try {
                if (req.getHeaders().containsKey("x-hashcash-result")) {
                    String tag = req.getHeaders().get("x-hashcash-result").toString();
                    if (tag == null || tag.isEmpty() || !gera.validateHashCash(tag)) {
                        req.abortWith(responseBuilder.build());
                    }
                } else {
                    req.abortWith(responseBuilder.build());
                }
            } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
                logger.severe(e.getMessage());
            }

        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        //throw new DemoiselleRestException("Não implementado", Response.Status.NOT_IMPLEMENTED.getStatusCode());
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method != null && classe != null && method.getAnnotation(HashCash.class) != null) {
            res.getHeaders().putSingle("x-hashcash-resource", gera.token());
            res.getHeaders().putSingle("x-hashcash-version", "1");
            res.getHeaders().putSingle("x-hashcash-bits", "25");
        }

    }

}
