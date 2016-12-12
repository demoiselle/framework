package org.demoiselle.jee.rest.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * 
 * @author SERPRO
 *
 */
@Provider
public class RestFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    	//TODO usar versao do maven
        responseContext.getHeaders().putSingle("Demoiselle", "3.0.0");
    }
}
