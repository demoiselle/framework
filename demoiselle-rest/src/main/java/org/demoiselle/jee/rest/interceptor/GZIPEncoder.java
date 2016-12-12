/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.interceptor;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.demoiselle.jee.rest.DemoiselleRestConfig;

/**
 *
 * @author SERPRO
 */
@Provider
public class GZIPEncoder implements WriterInterceptor {

    @Inject
    private DemoiselleRestConfig config;

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException, WebApplicationException {
        if (config.isGzipEnabled()) {
            GZIPOutputStream os = new GZIPOutputStream(ctx.getOutputStream());
            ctx.setOutputStream(os);
        }
        ctx.proceed();
    }
}
