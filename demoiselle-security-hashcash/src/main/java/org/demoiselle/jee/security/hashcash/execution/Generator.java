/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.security.hashcash.DemoiselleSecurityHashCashConfig;
import org.demoiselle.jee.security.store.LocalSecurityStore;
import org.demoiselle.jee.security.store.SecurityStore;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

/**
 * Emissão e verificação de desafios HashCash (proof-of-work).
 *
 * <p>Um desafio é um JWS HS256 assinado com o segredo configurado, vinculando:
 * o recurso protegido ({@code resource}), o instante de expiração ({@code exp},
 * derivado do TTL), um identificador único ({@code jti}) e a dificuldade
 * exigida ({@code bits}). O cliente resolve o proof-of-work encontrando um
 * {@code counter} tal que {@code SHA-256(challenge + ":" + counter)} tenha pelo
 * menos {@code bits} zeros iniciais.</p>
 *
 * <p>Garantias de segurança:</p>
 * <ul>
 *   <li>segredo forte obrigatório — o boot falha se ausente/curto;</li>
 *   <li>desafio assinado e à prova de adulteração (recurso/exp/jti/bits);</li>
 *   <li>SHA-256 (não SHA-1) e dificuldade consistente com o desafio;</li>
 *   <li>TTL: desafios expirados são rejeitados;</li>
 *   <li>proteção atômica contra replay do {@code jti} via {@link SecurityStore}.</li>
 * </ul>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class Generator {

    /** Minimum acceptable secret length (bytes) for HS256. */
    static final int MIN_SECRET_LENGTH = 32;

    /** Default proof-of-work difficulty (leading zero bits) when unset. */
    static final int DEFAULT_BITS = 20;

    /** Replay-protection namespace. */
    private static final String REPLAY_NS = "hashcash.jti";

    /** Weak, well-known secrets that must never be accepted. */
    private static final java.util.Set<String> FORBIDDEN_SECRETS =
            java.util.Set.of("demoiselle", "changeit", "secret", "password");

    @Inject
    private DemoiselleSecurityHashCashConfig config;

    @Inject
    private jakarta.enterprise.inject.Instance<SecurityStore> storeInstance;

    private Key key;
    private SecurityStore store;

    /** CDI constructor. */
    public Generator() {
    }

    /** Constructor for direct/non-CDI usage and tests. */
    public Generator(DemoiselleSecurityHashCashConfig config, SecurityStore store) {
        this.config = config;
        this.store = store;
        init();
    }

    @PostConstruct
    void init() {
        String secret = (config == null) ? null : config.getHashcashKey();
        validateSecret(secret);
        this.key = new HmacKey(secret.getBytes(StandardCharsets.UTF_8));
        if (this.store == null) {
            this.store = (storeInstance != null && storeInstance.isResolvable())
                    ? storeInstance.get() : new LocalSecurityStore();
        }
    }

    /**
     * Validates that a strong secret is configured, failing fast otherwise.
     *
     * @param secret the configured secret
     * @throws IllegalStateException when absent, too short or well-known/weak
     */
    static void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "demoiselle.security.hashcash.hashcashKey is required and must not be blank");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "demoiselle.security.hashcash.hashcashKey must be at least "
                            + MIN_SECRET_LENGTH + " bytes for HS256");
        }
        if (FORBIDDEN_SECRETS.contains(secret.trim().toLowerCase(java.util.Locale.ROOT))) {
            throw new IllegalStateException(
                    "demoiselle.security.hashcash.hashcashKey is a well-known weak value");
        }
    }

    private long ttlMillis() {
        Long ttl = (config == null) ? null : config.getTimetoLiveMilliseconds();
        return (ttl == null || ttl <= 0) ? 5_000L : ttl;
    }

    public int difficultyBits() {
        Integer bits = (config == null) ? null : config.getDifficultyBits();
        if (bits == null || bits <= 0) {
            return DEFAULT_BITS;
        }
        return Math.min(bits, 255);
    }

    /**
     * Emits a signed challenge bound to the given resource.
     *
     * @param resource the protected resource identifier (e.g. request path)
     * @return the compact JWS challenge, or {@code null} on signing failure
     */
    public String token(String resource) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setExpirationTime(NumericDate.fromMilliseconds(
                    System.currentTimeMillis() + ttlMillis()));
            claims.setIssuedAtToNow();
            claims.setGeneratedJwtId();
            claims.setStringClaim("resource", resource == null ? "" : resource);
            claims.setStringClaim("bits", Integer.toString(difficultyBits()));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(key);
            jws.setKeyIdHeaderValue("demoiselle-security-hashcash");
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            return jws.getCompactSerialization();
        } catch (JoseException ex) {
            throw new IllegalStateException("Failed to sign HashCash challenge", ex);
        }
    }

    /**
     * Backwards-compatible overload that emits a challenge with an empty
     * resource binding.
     *
     * @return the compact JWS challenge
     */
    public String token() {
        return token("");
    }

    /**
     * Parses and verifies a signed challenge, returning its claims.
     *
     * @param challenge the compact JWS challenge
     * @return the verified claims
     * @throws InvalidJwtException when signature/expiry are invalid
     */
    private JwtClaims verifyChallenge(String challenge) throws InvalidJwtException {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setRequireJwtId()
                .setVerificationKey(key)
                .setJwsAlgorithmConstraints(
                        org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT,
                        AlgorithmIdentifiers.HMAC_SHA256)
                .setRelaxVerificationKeyValidation()
                .build();
        return consumer.processToClaims(challenge);
    }

    /**
     * Validates a proof-of-work submission against its signed challenge.
     *
     * <p>The {@code cash} is the client's solution string, expected to be
     * {@code challenge + ":" + counter}. Validation enforces the challenge
     * signature, expiry, resource binding, the required difficulty (SHA-256
     * leading zero bits) and single-use of the challenge {@code jti}.</p>
     *
     * @param challenge the signed challenge issued by {@link #token(String)}
     * @param cash      the client's proof-of-work solution
     * @param resource  the resource being accessed (must match the challenge)
     * @return {@code true} if the proof is valid and not a replay
     * @throws NoSuchAlgorithmException if SHA-256 is unavailable
     */
    public boolean validateHashCash(String challenge, String cash, String resource)
            throws NoSuchAlgorithmException {
        if (challenge == null || challenge.isBlank() || cash == null || cash.isBlank()) {
            return false;
        }

        final JwtClaims claims;
        try {
            claims = verifyChallenge(challenge);
        } catch (InvalidJwtException ex) {
            // Bad signature, tampering or expiry.
            return false;
        }

        final String jti;
        final String boundResource;
        final int requiredBits;
        try {
            jti = claims.getJwtId();
            boundResource = claims.getStringClaimValue("resource");
            String bitsClaim = claims.getStringClaimValue("bits");
            requiredBits = (bitsClaim != null) ? Integer.parseInt(bitsClaim) : DEFAULT_BITS;
        } catch (MalformedClaimException | NumberFormatException ex) {
            return false;
        }

        // Resource binding: the solution must be for the resource we protect.
        String expectedResource = (resource == null) ? "" : resource;
        if (!expectedResource.equals(boundResource == null ? "" : boundResource)) {
            return false;
        }

        // The solution must embed the exact challenge it solves.
        if (!cash.startsWith(challenge + ":")) {
            return false;
        }

        // Proof-of-work: SHA-256 leading-zero-bit difficulty must be met.
        int computedBits = leadingZeroBits(sha256(cash));
        if (computedBits < requiredBits) {
            return false;
        }

        // Atomic single-use: reject replays of the same challenge jti.
        long remainingTtl = challengeRemainingMillis(claims);
        return store.markIfAbsent(REPLAY_NS, jti, Math.max(remainingTtl, 1_000L));
    }

    private static long challengeRemainingMillis(JwtClaims claims) {
        try {
            long exp = claims.getExpirationTime().getValueInMillis();
            return Math.max(exp - System.currentTimeMillis(), 0L);
        } catch (MalformedClaimException e) {
            return 0L;
        }
    }

    static byte[] sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Counts the number of leading zero bits in the digest.
     *
     * @param digest the hash bytes
     * @return leading zero bit count
     */
    static int leadingZeroBits(byte[] digest) {
        int result = 0;
        for (byte b : digest) {
            int v = b & 0xFF;
            if (v == 0) {
                result += 8;
                continue;
            }
            result += Integer.numberOfLeadingZeros(v) - 24;
            break;
        }
        return result;
    }
}
