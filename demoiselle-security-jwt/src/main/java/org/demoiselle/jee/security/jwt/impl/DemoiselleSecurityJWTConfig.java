/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.core.annotation.Name;

/**
 *
 * @author 70744416353
 */
@Configuration(prefix = "demoiselle.security.jwt")
public class DemoiselleSecurityJWTConfig implements Serializable {

    private static final long serialVersionUID = 638_435_989_235_076_782L;

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
     * Type server ou slave
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
