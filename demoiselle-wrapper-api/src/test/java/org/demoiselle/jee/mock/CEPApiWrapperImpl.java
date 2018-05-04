/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.mock;

import org.demoiselle.jee.wrapper.AbstractAPIWrapper;

/**
 *
 * @author 70744416353
 */
public class CEPApiWrapperImpl extends AbstractAPIWrapper<Cep, Integer> {

    @Override
    protected String resourceApi() {
        return "https://cep.demoiselle.estaleiro.serpro.gov.br/app/api/v1/ceps/";
    }

    @Override
    protected String resourceAuth() {
        return "https://cep.demoiselle.estaleiro.serpro.gov.br/app/api/auth/";
    }

    @Override
    protected String resourceUser() {
        return "admin@demoiselle.org";
    }

    @Override
    protected String resourcePassword() {
        return "123456";
    }

    @Override
    protected Boolean isAuthenticated() {
        return Boolean.FALSE;
    }
}
