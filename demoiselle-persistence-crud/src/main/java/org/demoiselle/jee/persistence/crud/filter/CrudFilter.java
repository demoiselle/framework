/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud.filter;

import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author 70744416353
 */
@Provider
@PreMatching
public class CrudFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo info;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        logger.info("Demoiselle Module: Crud");
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {

//        if (info.getResourceMethod() != null) {
//            Class<?> classe = info.getResourceClass();
//            if (classe != null) {
//                Crud crud = info.getClass().getAnnotation(Crud.class);
//                if (crud != null) {
//                    if (req.getMethod().equals("GET")) {
//                        System.out.println("for GET");
//                    }
//
//                    if (req.getMethod().equals("POST")) {
//                        System.out.println("for POST");
//                    }
//
//                    if (req.getMethod().equals("PUT")) {
//                        System.out.println("for PUT");
//                    }
//
//                    if (req.getMethod().equals("DELETE")) {
//                        System.out.println("for DELETE");
//                    }
//                }
//            }
//        }
    }
}
