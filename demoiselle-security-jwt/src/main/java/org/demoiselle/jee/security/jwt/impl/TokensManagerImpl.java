/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class TokensManagerImpl implements TokensManager {

    @Inject
    private HttpServletRequest httpRequest;

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    @Inject
    private Logger logger;

    @Inject
    private Token token;

    @Inject
    private Config config;

    @Inject
    private DemoisellePrincipal loggedUser;

    @PostConstruct
    public void init() {
        if (publicKey == null) {
            logger.info("Demoiselle Module - Security - JWT");
            try {

                if (config.getType() == null) {
                    throw new DemoiselleSecurityException("Escolha o tipo de autenticação, ver documentação", 500);
                }

                if (!config.getType().equalsIgnoreCase("slave") && !config.getType().equalsIgnoreCase("master")) {
                    throw new DemoiselleSecurityException("Os tipos de servidor são master ou slave, ver documentação", 500);
                }

                if (config.getType().equalsIgnoreCase("slave")) {
                    if (config.getPublicKey() == null || config.getPublicKey().isEmpty()) {
                        logger.warning("Coloque a chave pública no arquivo demoiselle-security-jwt.properties, ver documentação");
                        throw new DemoiselleSecurityException("Informe a chave pública no arquivo de configuração do projeto, ver documentação", 500);
                    } else {
                        publicKey = getPublic();
                    }
                }

                if (config.getType().equalsIgnoreCase("master")) {
                    privateKey = getPrivate();
                    publicKey = getPublic();
                }

            } catch (JoseException | InvalidKeySpecException ex) {
                logger.severe(ex.getMessage());
            } catch (NoSuchAlgorithmException ex) {
                logger.severe(ex.getMessage());
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
            }
        }
    }

    @Override
    public DemoisellePrincipal getUser() {
        if (token.getKey() != null && !token.getKey().isEmpty()) {
            try {
                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                        .setRequireExpirationTime() // the JWT must have an expiration time
                        .setAllowedClockSkewInSeconds(60) // allow some leeway in validating time based claims to account for clock skew
                        .setExpectedIssuer(config.getRemetente()) // whom the JWT needs to have been issued by
                        .setExpectedAudience(config.getDestinatario()) // to whom the JWT is intended for
                        .setVerificationKey(publicKey)
                        .build(); // create the JwtConsumer instance
                JwtClaims jwtClaims = jwtConsumer.processToClaims(token.getKey());
                loggedUser.setIdentity((String) jwtClaims.getClaimValue("identity"));
                loggedUser.setName((String) jwtClaims.getClaimValue("name"));
                loggedUser.setRoles((List) jwtClaims.getClaimValue("roles"));
                loggedUser.setPermissions((Map) jwtClaims.getClaimValue("permissions"));
                //loggedUser = new Gson().fromJson((String) jwtClaims.getClaimValue("user"), DemoisellePrincipal.class);
                String ip = httpRequest.getRemoteAddr();
                if (!ip.equalsIgnoreCase((String) jwtClaims.getClaimValue("ip"))) {
                    return null;
                }
                return loggedUser;
            } catch (InvalidJwtException ex) {
                loggedUser = null;
                token.setKey(null);
                logger.severe(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void setUser(DemoisellePrincipal user) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(config.getRemetente());
            claims.setAudience(config.getDestinatario());
            claims.setExpirationTimeMinutesInTheFuture(config.getTempo());
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(1);

            claims.setClaim("ip", httpRequest.getRemoteAddr());
            claims.setClaim("identity", (user.getIdentity()));
            claims.setClaim("name", (user.getName()));
            claims.setClaim("roles", (user.getRoles()));
            claims.setClaim("permissions", (user.getPermissions()));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(privateKey);
            jws.setKeyIdHeaderValue("demoiselle-security-jwt");
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            token.setKey(jws.getCompactSerialization());
            token.setType("JWT");
        } catch (JoseException ex) {
            logger.severe(ex.getMessage());
        }

    }

    @Override
    public boolean validate() {
        return getUser() != null;
    }

    private PrivateKey getPrivate() throws Exception {
        if (config.getPrivateKey() == null) {
            try {
                KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                keyGenerator.initialize(2048);
                KeyPair kp = keyGenerator.genKeyPair();
                publicKey = (PublicKey) kp.getPublic();
                privateKey = (PrivateKey) kp.getPrivate();
                config.setPrivateKey("-----BEGIN PRIVATE KEY-----" + Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "-----END PRIVATE KEY-----");
                config.setPublicKey("-----BEGIN PUBLIC KEY-----" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "-----END PUBLIC KEY-----");
                logger.warning("privateKey=" + config.getPrivateKey());
                logger.warning("publicKey=" + config.getPublicKey());
            } catch (NoSuchAlgorithmException ex) {
                logger.severe(ex.getMessage());
            }
        }
        byte[] keyBytes = Base64.getDecoder().decode(config.getPrivateKey().replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", ""));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey getPublic() throws Exception {
        RsaKeyUtil rsaKeyUtil = new RsaKeyUtil();
        return (PublicKey) rsaKeyUtil.fromPemEncoded(config.getPublicKey());
    }

}
