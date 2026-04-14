/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.api.ClaimsEnricher;
import org.demoiselle.jee.security.jwt.api.JwtTokenValidator;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

/**
 * Reusable JWT validation service that keeps raw token validation aligned with
 * the same rules used by the framework request filter.
 */
@ApplicationScoped
public class JwtTokenValidatorImpl implements JwtTokenValidator {

    @Inject
    private KeyRotationManager keyRotationManager;

    @Inject
    private DemoiselleSecurityJWTConfig config;

    @Inject
    private DemoiselleSecurityJWTMessages bundle;

    @Inject
    private TokenBlacklist tokenBlacklist;

    @Inject
    private Instance<ClaimsEnricher> claimsEnrichers;

    @Override
    public DemoiselleUser validate(String rawToken) {
        return validate(rawToken, null, null);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DemoiselleUser validate(String rawToken, String issuer, String audience) {
        String token = normalize(rawToken);
        if (token == null) {
            return null;
        }

        try {
            List<String> allowedAlgs = config.getAllowedAlgorithmsList();
            if (!allowedAlgs.isEmpty()) {
                try {
                    JsonWebSignature headerJws = new JsonWebSignature();
                    headerJws.setCompactSerialization(token);
                    String tokenAlg = headerJws.getAlgorithmHeaderValue();
                    if (tokenAlg == null || !allowedAlgs.contains(tokenAlg)) {
                        throw new DemoiselleSecurityException(bundle.algorithmNotAllowed(), Response.Status.UNAUTHORIZED.getStatusCode());
                    }
                } catch (JoseException e) {
                    throw new DemoiselleSecurityException(bundle.algorithmNotAllowed(), Response.Status.UNAUTHORIZED.getStatusCode());
                }
            }

            String kid = null;
            try {
                JsonWebSignature kidJws = new JsonWebSignature();
                kidJws.setCompactSerialization(token);
                kid = kidJws.getKeyIdHeaderValue();
            } catch (JoseException e) {
                kid = null;
            }

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(config.getClockSkewSeconds())
                    .setExpectedIssuer(issuer != null ? issuer : config.getIssuer())
                    .setExpectedAudience(audience != null ? audience : config.getAudience())
                    .setEvaluationTime(org.jose4j.jwt.NumericDate.now())
                    .setVerificationKey(keyRotationManager.getPublicKey(kid))
                    .build();
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            try {
                String jti = jwtClaims.getJwtId();
                if (jti != null && tokenBlacklist.isBlacklisted(jti)) {
                    throw new DemoiselleSecurityException(bundle.tokenBlacklisted(), Response.Status.UNAUTHORIZED.getStatusCode());
                }
            } catch (MalformedClaimException e) {
                // Ignore malformed JTI and keep retrocompatible behavior.
            }

            return buildUser(jwtClaims);
        } catch (InvalidJwtException ex) {
            throw new DemoiselleSecurityException(bundle.expired(), Response.Status.UNAUTHORIZED.getStatusCode(), ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private DemoiselleUser buildUser(JwtClaims jwtClaims) {
        DemoiselleUserImpl user = new DemoiselleUserImpl();
        user.init();

        user.setIdentity((String) jwtClaims.getClaimValue("identity"));
        user.setName((String) jwtClaims.getClaimValue("name"));

        List<String> roles = (List<String>) jwtClaims.getClaimValue("roles");
        if (roles != null) {
            roles.forEach(user::addRole);
        }

        Map<String, List<String>> permissions = (Map) jwtClaims.getClaimValue("permissions");
        if (permissions != null) {
            permissions.forEach((resource, operations) -> {
                if (operations != null) {
                    operations.forEach(operation -> user.addPermission(resource, operation));
                }
            });
        }

        Map<String, String> params = (Map) jwtClaims.getClaimValue("params");
        if (params != null) {
            params.forEach(user::addParam);
        }

        if (claimsEnrichers != null) {
            for (ClaimsEnricher enricher : claimsEnrichers) {
                enricher.extract(jwtClaims, user);
            }
        }

        return user;
    }

    private String normalize(String rawToken) {
        if (rawToken == null) {
            return null;
        }

        String token = rawToken.trim();
        if (token.isEmpty()) {
            return null;
        }

        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        return token.isEmpty() ? null : token;
    }
}
