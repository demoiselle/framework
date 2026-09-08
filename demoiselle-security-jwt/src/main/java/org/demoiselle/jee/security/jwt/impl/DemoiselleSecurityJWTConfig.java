/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;

/**
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.security.jwt")
public class DemoiselleSecurityJWTConfig implements Serializable {

    private static final long serialVersionUID = 638_435_989_235_076_782L;

    private String type;

    @ConfigurationSuppressLogger
    private String privateKey;

    private String publicKey;

    private Long timetoLiveMilliseconds;

    private String issuer;

    private String audience;

    private String algorithmIdentifiers;

    private Long refreshTokenTtlMilliseconds;

    private String allowedAlgorithms;

    private Integer clockSkewSeconds;

    private String activeKeyId;

    /**
     * Compact multi-key definition for local key rotation. Each entry is
     * {@code kid:publicKeyBase64Der[:privateKeyBase64Der[:validUntilEpochMillis]]}
     * and entries are separated by {@code ;}. The public/private material is the
     * base64 of the DER encoding (X.509 SubjectPublicKeyInfo / PKCS#8), i.e. a
     * PEM body without the {@code -----BEGIN...-----} envelope and without line
     * breaks. Private material may be omitted for verification-only (slave) keys.
     */
    @ConfigurationSuppressLogger
    private String keys;

    /**
     * How long (in seconds) the local key provider caches the parsed key set
     * before reloading from configuration. {@code 0} disables the refresh cache.
     */
    private Long keyRefreshSeconds;

    /**
     * Rotation window (in seconds) after a key's {@code validUntil} during which
     * it is still accepted for verification (never for signing). Lets tokens
     * signed with an outgoing key remain valid until they expire.
     */
    private Long keyRotationWindowSeconds;

    private String validationProfile;

    private String expectedType;

    private Long maxTokenAgeSeconds;

    private Boolean requireIssuer;

    private Boolean requireAudience;

    private Boolean requireIssuedAt;

    private Boolean requireJwtId;

    private Boolean requireSubject;

    /**
     * Compatibility profile (default): does not add any new validation
     * requirement over the historical behavior.
     */
    public static final String PROFILE_COMPAT = "compat";

    /**
     * Recommended profile: requires issuer, audience, {@code typ=JWT}, issued-at
     * (iat) and JWT id (jti), and enforces a maximum token age.
     */
    public static final String PROFILE_RECOMMENDED = "recommended";

    /**
     * Strict profile: everything from {@link #PROFILE_RECOMMENDED} plus a
     * required subject (sub) claim.
     */
    public static final String PROFILE_STRICT = "strict";

    public String getType() {
        return type;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Long getTimetoLiveMilliseconds() {
        return timetoLiveMilliseconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public String getAlgorithmIdentifiers() {
        return algorithmIdentifiers;
    }

    public Long getRefreshTokenTtlMilliseconds() {
        return refreshTokenTtlMilliseconds != null ? refreshTokenTtlMilliseconds : 86400000L;
    }

    public String getAllowedAlgorithms() {
        if (allowedAlgorithms != null && !allowedAlgorithms.isBlank()) {
            return allowedAlgorithms;
        }
        if (algorithmIdentifiers != null && !algorithmIdentifiers.isBlank()) {
            return algorithmIdentifiers;
        }
        return "RS256";
    }

    public Integer getClockSkewSeconds() {
        int value = clockSkewSeconds != null ? clockSkewSeconds : 60;
        if (value < 0) {
            return 60;
        }
        return value;
    }

    public String getActiveKeyId() {
        return activeKeyId;
    }

    /**
     * @return the raw compact multi-key definition, or {@code null} when not configured
     */
    public String getKeys() {
        return keys;
    }

    /**
     * @return the local key cache refresh interval in seconds (default 300, {@code 0} disables caching)
     */
    public long getKeyRefreshSeconds() {
        return keyRefreshSeconds != null && keyRefreshSeconds >= 0 ? keyRefreshSeconds : 300L;
    }

    /**
     * @return the rotation window in seconds during which an expired key still verifies (default 0)
     */
    public long getKeyRotationWindowSeconds() {
        return keyRotationWindowSeconds != null && keyRotationWindowSeconds >= 0 ? keyRotationWindowSeconds : 0L;
    }

    /**
     * Returns the allowed algorithms as a List of Strings.
     * Falls back to algorithmIdentifiers if allowedAlgorithms is not configured.
     */
    public List<String> getAllowedAlgorithmsList() {
        String algorithms = getAllowedAlgorithms();
        if (algorithms == null || algorithms.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(algorithms.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns the normalized validation profile: one of {@code compat},
     * {@code recommended} or {@code strict}. Unknown or blank values fall back
     * to {@code compat} to preserve the historical (default) behavior.
     *
     * @return the normalized profile name (never {@code null})
     */
    public String getValidationProfile() {
        if (validationProfile == null || validationProfile.isBlank()) {
            return PROFILE_COMPAT;
        }
        String normalized = validationProfile.trim().toLowerCase(java.util.Locale.ROOT);
        switch (normalized) {
            case PROFILE_RECOMMENDED:
                return PROFILE_RECOMMENDED;
            case PROFILE_STRICT:
                return PROFILE_STRICT;
            case PROFILE_COMPAT:
                return PROFILE_COMPAT;
            default:
                throw new IllegalArgumentException(
                        "Perfil de validacao JWT desconhecido: '" + validationProfile + "'. "
                        + "Valores aceitos: compat, recommended, strict.");
        }
    }

    private boolean isRecommendedOrStrict() {
        String profile = getValidationProfile();
        return PROFILE_RECOMMENDED.equals(profile) || PROFILE_STRICT.equals(profile);
    }

    /**
     * Returns the expected {@code typ} header value, or {@code null} when not
     * configured. In {@code recommended}/{@code strict} profiles the default is
     * {@code JWT}.
     *
     * @return the expected type, or {@code null}
     */
    public String getExpectedType() {
        if (expectedType != null && !expectedType.isBlank()) {
            return expectedType.trim();
        }
        if (isRecommendedOrStrict()) {
            return "JWT";
        }
        return null;
    }

    /**
     * Returns the configured maximum token age (in seconds). When not set, the
     * TTL ({@link #getTimetoLiveMilliseconds()}) converted to seconds is used as
     * fallback. Returns {@code null} only when neither is available.
     *
     * @return the maximum token age in seconds, or {@code null}
     */
    public Long getMaxTokenAgeSeconds() {
        if (maxTokenAgeSeconds != null && maxTokenAgeSeconds > 0) {
            return maxTokenAgeSeconds;
        }
        Long ttl = getTimetoLiveMilliseconds();
        if (ttl != null && ttl > 0) {
            return ttl / 1000L;
        }
        return null;
    }

    public Boolean getRequireIssuer() {
        return requireIssuer;
    }

    public Boolean getRequireAudience() {
        return requireAudience;
    }

    public Boolean getRequireIssuedAt() {
        return requireIssuedAt;
    }

    public Boolean getRequireJwtId() {
        return requireJwtId;
    }

    public Boolean getRequireSubject() {
        return requireSubject;
    }

    /**
     * Resolves whether the issuer claim is required, combining the active
     * profile floor with explicit overrides.
     *
     * <p>The profile acts as a <em>floor</em>: {@code recommended}/{@code strict}
     * require issuer. An explicit {@code true} activates the requirement in
     * {@code compat}; {@code false} never weakens a profile.</p>
     *
     * @return {@code true} when issuer validation must be enforced
     */
    public boolean isIssuerRequired() {
        return resolveRequirement(requireIssuer, isRecommendedOrStrict());
    }

    /**
     * @return {@code true} when audience validation must be enforced
     */
    public boolean isAudienceRequired() {
        return resolveRequirement(requireAudience, isRecommendedOrStrict());
    }

    /**
     * @return {@code true} when the issued-at (iat) claim must be present
     */
    public boolean isIssuedAtRequired() {
        return resolveRequirement(requireIssuedAt, isRecommendedOrStrict());
    }

    /**
     * @return {@code true} when the JWT id (jti) claim must be present
     */
    public boolean isJwtIdRequired() {
        return resolveRequirement(requireJwtId, isRecommendedOrStrict());
    }

    /**
     * @return {@code true} when the subject (sub) claim must be present
     */
    public boolean isSubjectRequired() {
        return resolveRequirement(requireSubject, PROFILE_STRICT.equals(getValidationProfile()));
    }

    /**
     * @return {@code true} when the {@code typ} header must be validated
     */
    public boolean isExpectedTypeRequired() {
        return getExpectedType() != null;
    }

    /**
     * Combines an explicit {@link Boolean} opt-in with the profile floor.
     * A profile requirement cannot be disabled: {@code TRUE} may activate a
     * requirement in {@code compat}, while {@code FALSE} only leaves the
     * profile floor unchanged.
     */
    private static boolean resolveRequirement(Boolean override, boolean profileFloor) {
        return profileFloor || Boolean.TRUE.equals(override);
    }

    /**
     * Validates that the configuration is internally consistent for the active
     * profile <em>before</em> any token is processed. When a requirement is
     * enabled but the corresponding configuration value is missing, a clear
     * {@link IllegalStateException} is raised.
     *
     * @throws IllegalStateException when issuer/audience are required but not configured
     */
    public void validateProfileConfiguration() {
        validateProfileConfiguration(issuer, audience);
    }

    /**
     * Validates the active profile using effective values supplied by the
     * caller, preserving the {@code validate(token, issuer, audience)} API.
     *
     * @param effectiveIssuer configured or explicitly supplied issuer
     * @param effectiveAudience configured or explicitly supplied audience
     * @throws IllegalStateException when a required value is missing
     */
    public void validateProfileConfiguration(String effectiveIssuer, String effectiveAudience) {
        if (isIssuerRequired() && (effectiveIssuer == null || effectiveIssuer.isBlank())) {
            throw new IllegalStateException(
                    "Perfil de validacao JWT '" + getValidationProfile()
                    + "' exige 'issuer', mas nenhum valor efetivo foi informado.");
        }
        if (isAudienceRequired() && (effectiveAudience == null || effectiveAudience.isBlank())) {
            throw new IllegalStateException(
                    "Perfil de validacao JWT '" + getValidationProfile()
                    + "' exige 'audience', mas nenhum valor efetivo foi informado.");
        }
    }

}
