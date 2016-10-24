/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.rest.DemoiselleRestConfig;

@Provider
public class RestFilter implements ContainerResponseFilter {

    @Inject
    private Logger logger;

    @Inject
    private DemoiselleRestConfig config;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Demoiselle", "3.0.0");
    }

    @PostConstruct
    public void init() {
        logger.info("Demoiselle Module: Rest");
        logger.log(Level.INFO, "GZip Enabled :{0}", config.isGzipEnabled());
    }

}
