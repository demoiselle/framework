/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import javax.annotation.Priority;
import javax.inject.Inject;
import static javax.ws.rs.Priorities.HEADER_DECORATOR;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

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
