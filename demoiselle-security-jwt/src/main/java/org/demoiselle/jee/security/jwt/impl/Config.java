/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.core.annotation.Name;

/**
 *
 * @author 70744416353
 */
@Configuration(resource = "demoiselle-security-jwt")
public class Config implements Serializable {

    private static final long serialVersionUID = 638435989235076782L;

    @Name("jwt.type")
    private String type;

    @Name("jwt.privateKey")
    private String privateKey;

    @Name("jwt.publicKey")
    private String publicKey;

    @Name("jwt.timetolive")
    private Float tempo;

    @Name("jwt.issuer")
    private String remetente;

    @Name("jwt.audience")
    private String destinatario;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Float getTempo() {
        return tempo;
    }

    public void setTempo(Float tempo) {
        this.tempo = tempo;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

}
