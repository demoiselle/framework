/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.ws.jaxrs.filter;

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
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.ws.jaxrs.annotation.Cache;
import org.demoiselle.jee.ws.jaxrs.annotation.Cors;

/**
 *
 * @author 70744416353
 */
@Provider
@PreMatching
public class JaxRsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    private Logger LOG;

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext requestContext) {
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response) {

        if (requestContext.getMethod().equals("GET")) {
            Cache max = info.getResourceMethod().getAnnotation(Cache.class);
            if (max != null) {
                response.getHeaders().putSingle("Cache-Control", max.value());
            }
        }
        
//        Cors cors = info.getResourceMethod().getAnnotation(Cors.class);
//            if (cors != null) {
//                response.getHeaders().putSingle("Cache-Control", max.value());
//            }

        response.getHeaders().putSingle("Demoiselle", "3.0.0");
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, DELETE");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type");
    }

    @PostConstruct
    public void init() {
        LOG.info("Demoiselle Module - Rest");
    }

}
