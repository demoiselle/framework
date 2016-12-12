/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.io.IOException;
import java.lang.reflect.Method;
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
//TODO usar priority
@Provider
public class CacheControlFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        if (req.getMethod().equals("GET")) {
            if (info.getResourceMethod() != null) {
                Method method = info.getResourceMethod();
                //TODO rever publicacao de codigo
                if (method != null) {
                    CacheControl max = info.getResourceMethod().getAnnotation(CacheControl.class);
                    if (max != null) {
                        res.getHeaders().putSingle("Cache-Control", max.value());
                    }
                }
            } 
            if (info.getResourceClass() != null) {
                @SuppressWarnings("rawtypes")
				Class classe = info.getResourceClass();
                if (classe != null) {
                    CacheControl max = info.getResourceClass().getAnnotation(CacheControl.class);
                    if (max != null) {
                        res.getHeaders().putSingle("Cache-Control", max.value());
                    }
                }
            }
        }
    }
}
