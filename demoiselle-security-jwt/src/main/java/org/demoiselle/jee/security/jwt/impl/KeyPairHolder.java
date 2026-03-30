/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;

/**
 * Application-scoped holder for RSA key pair used in JWT token operations.
 * Replaces the previous static fields in TokenManagerImpl, ensuring proper
 * CDI lifecycle management of cryptographic keys.
 *
 * @author SERPRO
 */
@ApplicationScoped
public class KeyPairHolder {

    private static final Logger logger = Logger.getLogger(KeyPairHolder.class.getName());

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Inject
    private DemoiselleSecurityJWTConfig config;

    @Inject
    private DemoiselleSecurityJWTMessages bundle;

    /**
     * Initializes the RSA key pair from configuration or generates a new pair.
     * Keys are loaded from demoiselle.properties; if not configured, a pair
     * is generated and logged for the administrator to copy into configuration.
     */
    @PostConstruct
    void init() {
        try {
            if (config.getType() == null) {
                throw new DemoiselleSecurityException(bundle.chooseType(), Response.Status.UNAUTHORIZED.getStatusCode());
            }

            if (!config.getType().equalsIgnoreCase(bundle.slave()) && !config.getType().equalsIgnoreCase(bundle.master())) {
                throw new DemoiselleSecurityException(bundle.notType(), Response.Status.UNAUTHORIZED.getStatusCode());
            }

            if (config.getType().equalsIgnoreCase(bundle.slave())) {
                if (config.getPublicKey() == null || config.getPublicKey().isEmpty()) {
                    throw new DemoiselleSecurityException(bundle.putKey(), Response.Status.UNAUTHORIZED.getStatusCode());
                } else {
                    publicKey = loadPublicKey();
                }
            }

            if (config.getType().equalsIgnoreCase(bundle.master())) {
                privateKey = loadPrivateKey();
                publicKey = loadPublicKey();
            }

        } catch (JoseException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new DemoiselleSecurityException(bundle.general(), Response.Status.UNAUTHORIZED.getStatusCode(), ex);
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Loads the private key from configuration. If not configured, generates
     * a new RSA 2048-bit key pair and logs the keys for the administrator.
     *
     * @return the private key
     * @throws NoSuchAlgorithmException if RSA algorithm is not available
     * @throws InvalidKeySpecException if the key spec is invalid
     */
    private PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (config.getPrivateKey() == null) {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(2_048);
            KeyPair kp = keyGenerator.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
            logger.warning("privateKey=Generated");
            logger.warning("publicKey=Generated");
            throw new DemoiselleSecurityException(bundle.putKey(), Response.Status.UNAUTHORIZED.getStatusCode());
        }
        byte[] keyBytes = java.util.Base64.getDecoder().decode(
                config.getPrivateKey()
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", ""));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Loads the public key from PEM-encoded configuration.
     *
     * @return the public key
     * @throws JoseException if the key cannot be parsed
     * @throws InvalidKeySpecException if the key spec is invalid
     */
    private PublicKey loadPublicKey() throws JoseException, InvalidKeySpecException {
        RsaKeyUtil rsaKeyUtil = new RsaKeyUtil();
        return rsaKeyUtil.fromPemEncoded(config.getPublicKey());
    }

}
