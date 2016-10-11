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
@Configuration(resource = "demoiselle-security-jwt", prefix = "")
public class Config implements Serializable {

    private static final long serialVersionUID = 638435989235076782L;

    @Name("type")
    private String type;

    @Name("privateKey")
    private String privateKey;

    @Name("publicKey")
    private String publicKey;

    @Name("timetolive")
    private Float tempo;

    @Name("issuer")
    private String remetente;

    @Name("audience")
    private String destinatario;

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     *
     * @param privateKey
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     *
     * @return
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     *
     * @param publicKey
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     *
     * @return
     */
    public Float getTempo() {
        return tempo;
    }

    /**
     *
     * @param tempo
     */
    public void setTempo(Float tempo) {
        this.tempo = tempo;
    }

    /**
     *
     * @return
     */
    public String getRemetente() {
        return remetente;
    }

    /**
     *
     * @param remetente
     */
    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    /**
     *
     * @return
     */
    public String getDestinatario() {
        return destinatario;
    }

    /**
     *
     * @param destinatario
     */
    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

}
