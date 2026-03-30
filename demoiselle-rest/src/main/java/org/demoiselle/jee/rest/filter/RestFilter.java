/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import static jakarta.ws.rs.Priorities.HEADER_DECORATOR;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.core.message.DemoiselleMessage;

/**
 *
 * @author SERPRO
 *
 */
@Provider
@Priority(HEADER_DECORATOR)
public class RestFilter implements ContainerResponseFilter {

    @Inject
    private DemoiselleMessage demoiselleMessage;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Demoiselle-Version", demoiselleMessage.frameworkName());
    }
}
