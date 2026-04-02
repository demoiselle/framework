/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

/**
 * Manages refresh token generation and validation.
 * Refresh tokens contain only minimal claims (sub, jti, exp, iss, aud, type)
 * and are used to obtain new access tokens without re-authentication.
 *
 * @author SERPRO
 */
@ApplicationScoped
public class RefreshTokenManager {

    @Inject
    private KeyRotationManager keyRotationManager;

    @Inject
    private TokenBlacklist tokenBlacklist;

    @Inject
    private DemoiselleSecurityJWTConfig config;

    @Inject
    private DemoiselleSecurityJWTMessages bundle;

    /**
     * Generates a refresh token containing only minimal claims.
     *
     * @param identity the user identity (sub claim)
     * @return compact JWT string of the refresh token
     */
    public String generateRefreshToken(String identity) {
        try {
            long expMillis = NumericDate.now().getValueInMillis() + config.getRefreshTokenTtlMilliseconds();

            JwtClaims claims = new JwtClaims();
            claims.setSubject(identity);
            claims.setGeneratedJwtId();
            claims.setExpirationTime(NumericDate.fromMilliseconds(expMillis));
            claims.setIssuer(config.getIssuer());
            claims.setAudience(config.getAudience());
            claims.setClaim("type", "refresh");

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(keyRotationManager.getActivePrivateKey());
            jws.setKeyIdHeaderValue(keyRotationManager.getActiveKeyId());

            List<String> allowedAlgs = config.getAllowedAlgorithmsList();
            String signingAlgorithm = (!allowedAlgs.isEmpty()) ? allowedAlgs.get(0) : config.getAlgorithmIdentifiers();
            jws.setAlgorithmHeaderValue(signingAlgorithm);

            return jws.getCompactSerialization();
        } catch (JoseException ex) {
            throw new DemoiselleSecurityException(bundle.general(), Response.Status.UNAUTHORIZED.getStatusCode(), ex);
        }
    }

    /**
     * Validates a refresh token and returns the identity (sub claim).
     * Checks expiration, signature, algorithm against allowedAlgorithms, and blacklist.
     *
     * @param refreshToken compact JWT string
     * @return the user identity from the sub claim
     * @throws DemoiselleSecurityException if invalid, expired, or blacklisted (HTTP 401)
     */
    public String validateRefreshToken(String refreshToken) {
        try {
            // Check algorithm against allowedAlgorithms
            List<String> allowedAlgs = config.getAllowedAlgorithmsList();
            if (!allowedAlgs.isEmpty()) {
                try {
                    JsonWebSignature headerJws = new JsonWebSignature();
                    headerJws.setCompactSerialization(refreshToken);
                    String tokenAlg = headerJws.getAlgorithmHeaderValue();
                    if (tokenAlg == null || !allowedAlgs.contains(tokenAlg)) {
                        throw new DemoiselleSecurityException(bundle.refreshTokenInvalid(), Response.Status.UNAUTHORIZED.getStatusCode());
                    }
                } catch (JoseException e) {
                    throw new DemoiselleSecurityException(bundle.refreshTokenInvalid(), Response.Status.UNAUTHORIZED.getStatusCode());
                }
            }

            // Extract kid from header for key rotation
            String kid = null;
            try {
                JsonWebSignature kidJws = new JsonWebSignature();
                kidJws.setCompactSerialization(refreshToken);
                kid = kidJws.getKeyIdHeaderValue();
            } catch (JoseException e) {
                // proceed with fallback
            }

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(config.getClockSkewSeconds())
                    .setExpectedIssuer(config.getIssuer())
                    .setExpectedAudience(config.getAudience())
                    .setVerificationKey(keyRotationManager.getPublicKey(kid))
                    .build();

            JwtClaims jwtClaims = jwtConsumer.processToClaims(refreshToken);

            // Check blacklist
            String jti = jwtClaims.getJwtId();
            if (jti != null && tokenBlacklist.isBlacklisted(jti)) {
                throw new DemoiselleSecurityException(bundle.tokenBlacklisted(), Response.Status.UNAUTHORIZED.getStatusCode());
            }

            // Verify it's a refresh token
            String type = (String) jwtClaims.getClaimValue("type");
            if (!"refresh".equals(type)) {
                throw new DemoiselleSecurityException(bundle.refreshTokenInvalid(), Response.Status.UNAUTHORIZED.getStatusCode());
            }

            return jwtClaims.getSubject();
        } catch (InvalidJwtException | MalformedClaimException ex) {
            throw new DemoiselleSecurityException(bundle.refreshTokenInvalid(), Response.Status.UNAUTHORIZED.getStatusCode(), ex);
        }
    }
}
