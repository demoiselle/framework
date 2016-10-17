/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.basic.impl;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.demoiselle.jee.security.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.security.interfaces.security.Token;
import org.demoiselle.jee.security.interfaces.security.TokensManager;

/**
 *
 * @author 70744416353
 */
@Dependent
public class TokensManagerImpl implements TokensManager {

    @Inject
    private DemoisellePrincipal loggedUser;

    @Inject
    private Token token;

    @Inject
    private Logger logger;

    @Inject
    private EntityManager entityManager;

    @Override
    public DemoisellePrincipal getUser() {
        try {

            byte[] asBytes = Base64.getDecoder().decode(token.getKey());
            String login = new String(asBytes, "utf-8");
            loggedUser = (DemoisellePrincipal) entityManager.createNativeQuery("select * from usuario where usuario = " + login.split(":")[0] + " senha = " + login.split(":")[1]).getResultList().get(0);

        } catch (UnsupportedEncodingException ex) {
            logger.severe(ex.getMessage());
        }

        return loggedUser;
    }

    @Override
    public void setUser(DemoisellePrincipal user) {
        loggedUser = user;
    }

    @Override
    public boolean validate() {
        return getUser() != null;
    }

}
