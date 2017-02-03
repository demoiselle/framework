/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Priority;
import static javax.ws.rs.Priorities.HEADER_DECORATOR;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;

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

    private static final ConcurrentHashMap<String, Long> repo = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        throw new DemoiselleRestException("Não implementado", Response.Status.NOT_IMPLEMENTED.getStatusCode());
//        Method method = info.getResourceMethod();
//        Class<?> classe = info.getResourceClass();
//
//        if (method != null && classe != null && method.getAnnotation(HashCash.class) != null) {
//            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.PARTIAL_CONTENT);
//            try {
//                if (req.getHeaders().containsKey("x-hashcash-result")) {
//                    String tag = req.getHeaders().get("x-hashcash-result").toString();
//                    if (tag != null && !tag.isEmpty()) {
//                        HashCash hashcash = new HashCash(tag.replace("[", "").replace("]", ""));
//                        Long time = repo.get(hashcash.getResource());
//
//                        if (!hashcash.getResource().equals(hashcash.getResource())) {
//                            req.abortWith(responseBuilder.build());
//                        }
////                        if (System.currentTimeMillis() <= (time + 5000)) {
//                        //req.abortWith(responseBuilder.build());
////                        }
//                        //HashCash.mintCash(hashcash, HEADER_DECORATOR)
//                    } else {
//                        req.abortWith(responseBuilder.build());
//                    }
//                    repo.remove(tag);
//                } else {
//                    req.abortWith(responseBuilder.build());
//                }
//
//            } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
//                logger.severe(e.getMessage());
//            }
//
//        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        throw new DemoiselleRestException("Não implementado", Response.Status.NOT_IMPLEMENTED.getStatusCode());
//        Method method = info.getResourceMethod();
//        Class<?> classe = info.getResourceClass();
//
//        if (method != null && classe != null && method.getAnnotation(HashCash.class) != null) {
//            String id = UUID.randomUUID().toString();
//            Long time = System.currentTimeMillis();
//            repo.putIfAbsent(id, time);
//            res.getHeaders().putSingle("x-hashcash-resource", id);
//            res.getHeaders().putSingle("x-hashcash-version", "1");
//            res.getHeaders().putSingle("x-hashcash-bits", "25");
//            res.getHeaders().putSingle("x-hashcash-time", time);
//        }

    }

}
