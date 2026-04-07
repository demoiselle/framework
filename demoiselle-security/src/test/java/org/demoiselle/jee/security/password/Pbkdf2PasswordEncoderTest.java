/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.password;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

/**
 * Testes unitários e de propriedade para {@link Pbkdf2PasswordEncoder}.
 *
 * @author Demoiselle Framework
 */
class Pbkdf2PasswordEncoderTest {

    private Pbkdf2PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new Pbkdf2PasswordEncoder();
    }

    // --- Testes unitários ---

    @Test
    void encode_shouldReturnNonNullHash() {
        String hash = encoder.encode("password123");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void encode_shouldStartWithPBKDF2Prefix() {
        String hash = encoder.encode("test");
        assertTrue(hash.startsWith("PBKDF2:"), "Hash should start with PBKDF2: prefix");
    }

    @Test
    void encode_shouldHaveFourParts() {
        String hash = encoder.encode("test");
        String[] parts = hash.split(":", 4);
        assertEquals(4, parts.length, "Hash should have 4 parts: prefix:iterations:salt:hash");
    }

    @Test
    void encode_shouldUse210000Iterations() {
        String hash = encoder.encode("test");
        String[] parts = hash.split(":", 4);
        assertEquals("210000", parts[1]);
    }

    @Test
    void encode_shouldProduceDifferentHashesForSamePassword() {
        String hash1 = encoder.encode("samePassword");
        String hash2 = encoder.encode("samePassword");
        assertNotEquals(hash1, hash2, "Each encode should produce a different hash (random salt)");
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String hash = encoder.encode("correctPassword");
        assertTrue(encoder.matches("correctPassword", hash));
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String hash = encoder.encode("correctPassword");
        assertFalse(encoder.matches("wrongPassword", hash));
    }

    @Test
    void matches_shouldReturnFalseForNullPassword() {
        String hash = encoder.encode("test");
        assertFalse(encoder.matches(null, hash));
    }

    @Test
    void matches_shouldReturnFalseForNullHash() {
        assertFalse(encoder.matches("test", null));
    }

    @Test
    void matches_shouldReturnFalseForMalformedHash() {
        assertFalse(encoder.matches("test", "not-a-valid-hash"));
        assertFalse(encoder.matches("test", "PBKDF2:abc:salt:hash"));
        assertFalse(encoder.matches("test", "WRONG:210000:salt:hash"));
        assertFalse(encoder.matches("test", ""));
    }

    @Test
    void encode_shouldThrowForNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> encoder.encode(null));
    }

    @Test
    void encode_shouldThrowForEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> encoder.encode(""));
    }

    // --- Testes de propriedade (jqwik) ---

    @Property(tries = 200)
    void encodeAndMatchShouldAlwaysSucceedForAnyPassword(
            @ForAll @StringLength(min = 1, max = 64) String password) {
        String hash = encoder.encode(password);
        assertTrue(encoder.matches(password, hash),
                "matches() must return true for the password that was encoded");
    }

    @Property(tries = 200)
    void differentPasswordsShouldNotMatch(
            @ForAll @StringLength(min = 1, max = 32) String password1,
            @ForAll @StringLength(min = 1, max = 32) String password2) {
        Assume.that(!password1.equals(password2));
        String hash = encoder.encode(password1);
        assertFalse(encoder.matches(password2, hash),
                "matches() must return false for a different password");
    }

    @Property(tries = 100)
    void encodeShouldAlwaysProduceValidFormat(
            @ForAll @StringLength(min = 1, max = 64) String password) {
        String hash = encoder.encode(password);

        String[] parts = hash.split(":", 4);
        assertEquals(4, parts.length, "Hash must have 4 parts");
        assertEquals("PBKDF2", parts[0], "Prefix must be PBKDF2");
        assertEquals("210000", parts[1], "Iterations must be 210000");
        assertFalse(parts[2].isEmpty(), "Salt must not be empty");
        assertFalse(parts[3].isEmpty(), "Hash must not be empty");
    }

    @Property(tries = 100)
    void eachEncodeShouldProduceUniqueSalt(
            @ForAll @StringLength(min = 1, max = 32) String password) {
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        String salt1 = hash1.split(":", 4)[2];
        String salt2 = hash2.split(":", 4)[2];

        assertNotEquals(salt1, salt2, "Each encode must use a unique random salt");
    }

    @Property(tries = 50)
    void unicodePasswordsShouldWork(
            @ForAll("unicodePasswords") String password) {
        String hash = encoder.encode(password);
        assertTrue(encoder.matches(password, hash),
                "Unicode passwords must encode and match correctly");
    }

    @Provide
    Arbitrary<String> unicodePasswords() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(32)
                .withCharRange('\u0080', '\u07FF')  // Latin Extended, Greek, Cyrillic, etc.
                .filter(s -> !s.isEmpty());
    }
}
