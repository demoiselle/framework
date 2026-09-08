/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the JWT validation profile resolution logic in
 * {@link DemoiselleSecurityJWTConfig}. These exercise pure configuration logic
 * (no CDI/token processing), setting private fields reflectively to simulate
 * property injection.
 *
 * @author SERPRO
 */
class ValidationProfileConfigTest {

    private static DemoiselleSecurityJWTConfig config(Object... pairs) {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        for (int i = 0; i < pairs.length; i += 2) {
            set(cfg, (String) pairs[i], pairs[i + 1]);
        }
        return cfg;
    }

    private static void set(DemoiselleSecurityJWTConfig cfg, String field, Object value) {
        try {
            Field f = DemoiselleSecurityJWTConfig.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(cfg, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void defaultProfileIsCompat() {
        DemoiselleSecurityJWTConfig cfg = config();
        assertEquals(DemoiselleSecurityJWTConfig.PROFILE_COMPAT, cfg.getValidationProfile());
    }

    @Test
    void profileIsNormalizedCaseInsensitive() {
        assertEquals(DemoiselleSecurityJWTConfig.PROFILE_RECOMMENDED,
                config("validationProfile", "  Recommended ").getValidationProfile());
        assertEquals(DemoiselleSecurityJWTConfig.PROFILE_STRICT,
                config("validationProfile", "STRICT").getValidationProfile());
        assertEquals(DemoiselleSecurityJWTConfig.PROFILE_COMPAT,
                config("validationProfile", "compat").getValidationProfile());
    }

    @Test
    void unknownProfileIsRejected() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "banana");
        assertThrows(IllegalArgumentException.class, cfg::getValidationProfile);
    }

    @Test
    void compatDoesNotRequireAnything() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "compat");
        assertFalse(cfg.isIssuerRequired());
        assertFalse(cfg.isAudienceRequired());
        assertFalse(cfg.isIssuedAtRequired());
        assertFalse(cfg.isJwtIdRequired());
        assertFalse(cfg.isSubjectRequired());
        assertFalse(cfg.isExpectedTypeRequired());
    }

    @Test
    void recommendedRequiresIssuerAudienceTypIatJti() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "recommended");
        assertTrue(cfg.isIssuerRequired());
        assertTrue(cfg.isAudienceRequired());
        assertTrue(cfg.isIssuedAtRequired());
        assertTrue(cfg.isJwtIdRequired());
        assertTrue(cfg.isExpectedTypeRequired());
        assertEquals("JWT", cfg.getExpectedType());
        // subject is only required in strict
        assertFalse(cfg.isSubjectRequired());
    }

    @Test
    void strictRequiresSubjectOnTopOfRecommended() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "strict");
        assertTrue(cfg.isIssuerRequired());
        assertTrue(cfg.isAudienceRequired());
        assertTrue(cfg.isIssuedAtRequired());
        assertTrue(cfg.isJwtIdRequired());
        assertTrue(cfg.isExpectedTypeRequired());
        assertTrue(cfg.isSubjectRequired());
    }

    @Test
    void trueOverrideActivatesRequirementInCompat() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "compat",
                "requireJwtId", Boolean.TRUE,
                "requireIssuedAt", Boolean.TRUE);
        assertTrue(cfg.isJwtIdRequired());
        assertTrue(cfg.isIssuedAtRequired());
        // Others remain off
        assertFalse(cfg.isIssuerRequired());
        assertFalse(cfg.isSubjectRequired());
    }

    @Test
    void falseOverrideDoesNotWeakenRecommendedProfile() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "requireIssuer", Boolean.FALSE,
                "requireAudience", Boolean.FALSE);
        assertTrue(cfg.isIssuerRequired());
        assertTrue(cfg.isAudienceRequired());
        assertTrue(cfg.isIssuedAtRequired());
        assertTrue(cfg.isJwtIdRequired());
    }

    @Test
    void expectedTypeExplicitOverridesDefault() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "expectedType", "at+jwt");
        assertEquals("at+jwt", cfg.getExpectedType());
        assertTrue(cfg.isExpectedTypeRequired());
    }

    @Test
    void expectedTypeNullInCompatByDefault() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "compat");
        assertNull(cfg.getExpectedType());
    }

    @Test
    void maxTokenAgeUsesConfiguredValueWhenPresent() {
        DemoiselleSecurityJWTConfig cfg = config(
                "maxTokenAgeSeconds", 300L,
                "timetoLiveMilliseconds", 9_999_999L);
        assertEquals(300L, cfg.getMaxTokenAgeSeconds());
    }

    @Test
    void maxTokenAgeFallsBackToTtlInSeconds() {
        DemoiselleSecurityJWTConfig cfg = config("timetoLiveMilliseconds", 120_000L);
        assertEquals(120L, cfg.getMaxTokenAgeSeconds());
    }

    @Test
    void maxTokenAgeNullWhenNeitherConfigured() {
        DemoiselleSecurityJWTConfig cfg = config();
        assertNull(cfg.getMaxTokenAgeSeconds());
    }

    @Test
    void validateProfileConfigurationFailsWhenIssuerMissingInRecommended() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "audience", "web");
        IllegalStateException ex = assertThrows(IllegalStateException.class, cfg::validateProfileConfiguration);
        assertTrue(ex.getMessage().contains("issuer"));
    }

    @Test
    void validateProfileConfigurationFailsWhenAudienceMissingInStrict() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "strict",
                "issuer", "STORE");
        IllegalStateException ex = assertThrows(IllegalStateException.class, cfg::validateProfileConfiguration);
        assertTrue(ex.getMessage().contains("audience"));
    }

    @Test
    void validateProfileConfigurationPassesInCompatWithoutIssuerAudience() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "compat");
        // Should not throw
        cfg.validateProfileConfiguration();
    }

    @Test
    void validateProfileConfigurationPassesWhenIssuerAudiencePresent() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web");
        cfg.validateProfileConfiguration();
    }

    @Test
    void trueOverrideForIssuerRequiresConfigurationEvenInCompat() {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "compat",
                "requireIssuer", Boolean.TRUE);
        assertThrows(IllegalStateException.class, cfg::validateProfileConfiguration);
    }

    @Test
    void explicitEffectiveIssuerAndAudienceSatisfyRecommendedProfile() {
        DemoiselleSecurityJWTConfig cfg = config("validationProfile", "recommended");
        cfg.validateProfileConfiguration("STORE", "web");
    }
}
