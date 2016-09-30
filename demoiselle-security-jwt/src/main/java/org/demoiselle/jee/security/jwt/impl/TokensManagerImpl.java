/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class TokensManagerImpl implements TokensManager {

    @Inject
    private HttpServletRequest httpRequest;

    private static RsaJsonWebKey rsaJsonWebKey;

    @Inject
    private Logger logger;

    @Inject
    private Token token;

//    @Inject
//    private Config config;
    @Inject
    private DemoisellePrincipal loggedUser;

    //@PostConstruct
    public TokensManagerImpl() {
        if (rsaJsonWebKey == null) {
//            logger.info("Demoiselle Module - Security - JWT");
            try {

//                if (config.getType() == null) {
//                    throw new DemoiselleSecurityException("Escolha o tipo de autenticação, ver documentação", 500);
//                }
//
//                if (config.getType().equalsIgnoreCase("slave") && (config.getPublicKey() == null || config.getPublicKey().isEmpty())) {
//                    throw new DemoiselleSecurityException("Informe a chave pública no arquivo de configuração do projeto, ver documentação", 500);
//                } else {
//                    rsaJsonWebKey = (RsaJsonWebKey) RsaJsonWebKey.Factory.newPublicJwk(config.getPrivateKey());
//                }
//
//                if (config.getType().equalsIgnoreCase("master") && (config.getPrivateKey() == null || config.getPrivateKey().isEmpty())) {
//                    throw new DemoiselleSecurityException("Informe a chave privada no arquivo de configuração do projeto, ver documentação", 500);
//                } else {
//                    rsaJsonWebKey = (RsaJsonWebKey) RsaJsonWebKey.Factory.newPublicJwk(config.getPublicKey());
//                }
                rsaJsonWebKey = (RsaJsonWebKey) RsaJsonWebKey.Factory.newPublicJwk(RsaJwkGenerator.generateJwk(2048).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
                rsaJsonWebKey.setKeyId("demoiselle-security-jwt");
            } catch (JoseException ex) {
                //  logger.severe(ex.getMessage());
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
                        .setExpectedIssuer("demoiselle") // whom the JWT needs to have been issued by
                        .setExpectedAudience("demoiselle") // to whom the JWT is intended for
                        .setVerificationKey(rsaJsonWebKey.getPublicKey())
                        .build(); // create the JwtConsumer instance
                JwtClaims jwtClaims = jwtConsumer.processToClaims(token.getKey());
                loggedUser.setId((String) jwtClaims.getClaimValue("id"));
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
            claims.setIssuer("demoiselle");
            claims.setAudience("demoiselle");
            claims.setExpirationTimeMinutesInTheFuture(720);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(1);

            claims.setClaim("ip", httpRequest.getRemoteAddr());
            claims.setClaim("id", (user.getId()));
            claims.setClaim("name", (user.getName()));
            claims.setClaim("roles", (user.getRoles()));
            claims.setClaim("permissions", (user.getPermissions()));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(rsaJsonWebKey.getRsaPrivateKey());
            jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
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

    @Override
    public PublicKey getPublicKey() {
        return rsaJsonWebKey.getPublicKey();
    }
}
