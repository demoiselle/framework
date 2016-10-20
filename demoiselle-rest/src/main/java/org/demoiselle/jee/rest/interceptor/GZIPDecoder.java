/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest.interceptor;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.demoiselle.jee.rest.DemoiselleRestConfig;

/**
 *
 * @author 70744416353
 */
@Provider
public class GZIPDecoder implements ReaderInterceptor {

    @Inject
    private DemoiselleRestConfig config;

    public Object aroundReadFrom(ReaderInterceptorContext ctx) throws IOException {

        if (config.isGzipEnabled()) {
            String encoding = ctx.getHeaders().getFirst("Content-Encoding");
            if (!"gzip".equalsIgnoreCase(encoding)) {
                return ctx.proceed();
            }
            GZIPInputStream is = new GZIPInputStream(ctx.getInputStream());
            ctx.setInputStream(is);
        }
        return ctx.proceed();
    }
}
