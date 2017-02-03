/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import static javax.ws.rs.Priorities.HEADER_DECORATOR;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.rest.annotation.CacheControl;

/**
 *
 * @author SERPRO
 */
@Provider
@Priority(HEADER_DECORATOR)
public class CacheControlFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        if (req.getMethod().equals("GET")) {
            Method method = info.getResourceMethod();
            if (method != null) {
                CacheControl max = method.getAnnotation(CacheControl.class);
                if (max != null) {
                    res.getHeaders().putSingle("Cache-Control", max.value());
                }
            }
        }
    }
}
