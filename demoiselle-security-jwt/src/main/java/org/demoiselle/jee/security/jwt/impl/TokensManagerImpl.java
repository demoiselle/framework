/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import com.google.gson.Gson;
import java.security.Key;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.interfaces.security.Token;
import org.demoiselle.jee.core.interfaces.security.TokensManager;
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
@Dependent
public class TokensManagerImpl implements TokensManager {

    @Inject
    private HttpServletRequest httpRequest;

    private RsaJsonWebKey rsaJsonWebKey;

    @Inject
    private Logger logger;

    @Inject
    private Token token;

    @Inject
    private DemoisellePrincipal loggedUser;

    public TokensManagerImpl() throws JoseException {
        RsaJsonWebKey chave = RsaJwkGenerator.generateJwk(2048);
        logger.info("Se você quiser usar sua app em cluster, coloque o parametro jwt.key no app.properties e reinicie a aplicacao");
        logger.log(Level.INFO, "jwt.key={0}", chave);
        logger.info("Se você não usar esse parametro, a cada reinicialização será gerada uma nova chave privada, isso inviabiliza o uso em cluster ");
        rsaJsonWebKey = (RsaJsonWebKey) RsaJsonWebKey.Factory.newPublicJwk((Key) chave);
        rsaJsonWebKey.setKeyId("demoiselle-security-jwt");
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
                        .setVerificationKey(rsaJsonWebKey.getKey()) // verify the signature with the public key
                        .build(); // create the JwtConsumer instance
                JwtClaims jwtClaims = jwtConsumer.processToClaims(token.getKey());
                loggedUser = new Gson().fromJson((String) jwtClaims.getClaimValue("user"), DemoisellePrincipal.class);
                String ip = httpRequest.getRemoteAddr();
                if (!ip.equalsIgnoreCase((String) jwtClaims.getClaimValue("ip"))) {
                    return null;
                }
            } catch (InvalidJwtException ex) {
                logger.severe(ex.getMessage());
            }
        }
        return loggedUser;
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
            claims.setClaim("user", new Gson().toJson(user));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(rsaJsonWebKey.getPrivateKey());
            jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            token.setKey(jws.getCompactSerialization());
        } catch (JoseException ex) {
            logger.severe(ex.getMessage());
        }

    }

    @Override
    public boolean validate() {
        return true;
    }

}
