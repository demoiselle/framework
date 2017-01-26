/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.pow.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Priority;
import static javax.ws.rs.Priorities.HEADER_DECORATOR;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.security.pow.annotation.ProofOfWork;

/**
 *
 * @author SERPRO
 */
@Provider
@PreMatching
@Priority(HEADER_DECORATOR)
public class PowFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(PowFilter.class.getName());

    @Context
    private ResourceInfo info;

    private static final ConcurrentHashMap<String, Long> repo = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext req) throws IOException {

        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method != null && classe != null && method.getAnnotation(ProofOfWork.class) != null) {
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.PARTIAL_CONTENT);
            try {
                if (req.getHeaders().containsKey("hashcash")) {
                    String hashcash = req.getHeaders().get("hashcash").toString();
                    if (hashcash != null && !hashcash.isEmpty() && repo.containsKey(hashcash)) {
//                        Long time = repo.get(hashcash);
//                        if (System.currentTimeMillis() <= (time + 5000)) {
                        req.abortWith(responseBuilder.build());
//                        }
                        //HashCash.mintCash(hashcash, HEADER_DECORATOR)
                    } else {
                        req.abortWith(responseBuilder.build());
                    }
                    repo.remove(hashcash);
                }

            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        Method method = info.getResourceMethod();
        Class<?> classe = info.getResourceClass();

        if (method != null && classe != null && method.getAnnotation(ProofOfWork.class) != null) {
            String id = UUID.randomUUID().toString();
            repo.putIfAbsent(id, System.currentTimeMillis());
            res.getHeaders().putSingle("hash", id);
        }

    }

}
