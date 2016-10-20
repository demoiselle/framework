/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author 70744416353
 */
@Provider
public class GZIPEncoder implements WriterInterceptor {

    @Inject
    private DemoiselleRestConfig config;

    /**
     *
     * @param ctx
     * @throws IOException
     * @throws WebApplicationException
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException, WebApplicationException {
        if (config.isGzipEnabled()) {
            GZIPOutputStream os = new GZIPOutputStream(ctx.getOutputStream());
            ctx.setOutputStream(os);
        }
        ctx.proceed();
    }
}
