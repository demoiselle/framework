/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.persistence.crud.DemoiselleCrudConfig;
import org.demoiselle.jee.persistence.crud.ResultSet;
import org.demoiselle.jee.rest.annotation.CacheControl;

/**
 *
 * @author 70744416353
 */
@Provider
public class CrudFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo info;

    @Inject
    private DemoiselleCrudConfig config;

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        if (config.isPatternsEnabled()) {
            if (res.getEntity() instanceof ResultSet) {
                ResultSet rs = (ResultSet) res.getEntity();
                res.getHeaders().putSingle("Content-Range", "" + rs.getInit() + "-" + rs.getQtde() + "/" + rs.getTotal());
                res.getHeaders().putSingle("Accept-Range", "" + config.getAcceptRange());
                res.setEntity(rs.getContent());
                if ((rs.getInit() + rs.getQtde()) == rs.getTotal()) {
                    res.setStatus(200);
                } else {
                    res.setStatus(206);
                }
            }
        }
    }
}
