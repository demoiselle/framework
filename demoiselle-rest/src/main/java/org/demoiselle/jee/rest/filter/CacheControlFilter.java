/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import jakarta.annotation.Priority;
import static jakarta.ws.rs.Priorities.HEADER_DECORATOR;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
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
