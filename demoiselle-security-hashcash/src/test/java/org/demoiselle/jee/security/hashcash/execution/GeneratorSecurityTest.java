/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import org.demoiselle.jee.security.hashcash.DemoiselleSecurityHashCashConfig;
import org.demoiselle.jee.security.store.LocalSecurityStore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for {@link Generator}: strong-secret enforcement, signed
 * resource-bound challenges, SHA-256 difficulty, expiration, tampering and
 * atomic replay protection.
 */
class GeneratorSecurityTest {

    private static final String STRONG_SECRET = "0123456789abcdef0123456789abcdef"; // 32 bytes

    private static DemoiselleSecurityHashCashConfig config(String secret, long ttlMs, int bits) throws Exception {
        DemoiselleSecurityHashCashConfig cfg = new DemoiselleSecurityHashCashConfig();
        set(cfg, "hashcashKey", secret);
        set(cfg, "timetoLiveMilliseconds", ttlMs);
        set(cfg, "difficultyBits", bits);
        return cfg;
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Brute-forces a counter so that SHA-256(challenge:counter) has >= bits leading zeros. */
    private static String solve(String challenge, int bits) throws Exception {
        for (long c = 0; c < 100_000_000L; c++) {
            String candidate = challenge + ":" + c;
            if (Generator.leadingZeroBits(Generator.sha256(candidate)) >= bits) {
                return candidate;
            }
        }
        throw new AssertionError("No solution found (bits too high for test)");
    }

    // --- Strong-secret enforcement ---

    @Test
    void bootFailsWithoutSecret() {
        assertThrows(IllegalStateException.class, () -> Generator.validateSecret(null));
        assertThrows(IllegalStateException.class, () -> Generator.validateSecret("   "));
    }

    @Test
    void bootFailsWithShortSecret() {
        assertThrows(IllegalStateException.class, () -> Generator.validateSecret("short"));
    }

    @Test
    void bootFailsWithWellKnownWeakSecret() {
        assertThrows(IllegalStateException.class, () -> Generator.validateSecret("Demoiselle"));
    }

    @Test
    void bootSucceedsWithStrongSecret() {
        assertDoesNotThrow(() -> Generator.validateSecret(STRONG_SECRET));
    }

    // --- Happy path ---

    @Test
    void validSolutionIsAccepted() throws Exception {
        int bits = 8;
        Generator g = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String challenge = g.token("GET orders");
        String cash = solve(challenge, bits);
        assertTrue(g.validateHashCash(challenge, cash, "GET orders"));
    }

    // --- Replay protection ---

    @Test
    void replayOfSameChallengeIsRejected() throws Exception {
        int bits = 8;
        Generator g = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String challenge = g.token("GET orders");
        String cash = solve(challenge, bits);
        assertTrue(g.validateHashCash(challenge, cash, "GET orders"), "first use accepted");
        assertFalse(g.validateHashCash(challenge, cash, "GET orders"), "replay rejected");
    }

    // --- Tampering ---

    @Test
    void tamperedChallengeIsRejected() throws Exception {
        int bits = 8;
        Generator g = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String challenge = g.token("GET orders");
        String cash = solve(challenge, bits);
        // Flip a character in the JWS payload/signature.
        String tampered = challenge.substring(0, challenge.length() - 2)
                + (challenge.endsWith("A") ? "B" : "A") + challenge.substring(challenge.length() - 1);
        assertFalse(g.validateHashCash(tampered, cash.replace(challenge, tampered), "GET orders"));
    }

    @Test
    void challengeSignedWithDifferentSecretIsRejected() throws Exception {
        int bits = 8;
        Generator issuer = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String otherSecret = "ffffffffffffffffffffffffffffffff"; // 32 bytes, different
        Generator verifier = new Generator(config(otherSecret, 60_000, bits), new LocalSecurityStore());

        String challenge = issuer.token("GET orders");
        String cash = solve(challenge, bits);
        assertFalse(verifier.validateHashCash(challenge, cash, "GET orders"),
                "challenge from a foreign secret must not verify");
    }

    // --- Resource binding ---

    @Test
    void solutionForDifferentResourceIsRejected() throws Exception {
        int bits = 8;
        Generator g = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String challenge = g.token("GET orders");
        String cash = solve(challenge, bits);
        assertFalse(g.validateHashCash(challenge, cash, "GET invoices"),
                "resource mismatch must be rejected");
    }

    // --- Difficulty ---

    @Test
    void insufficientDifficultyIsRejected() throws Exception {
        int bits = 16;
        Generator g = new Generator(config(STRONG_SECRET, 60_000, bits), new LocalSecurityStore());
        String challenge = g.token("GET orders");
        // Solve for a lower difficulty than required.
        String weak = challenge + ":" + 0; // almost certainly < 16 leading zero bits
        // ensure our weak solution truly is below the bar
        assumeBelow(weak, bits);
        assertFalse(g.validateHashCash(challenge, weak, "GET orders"));
    }

    private static void assumeBelow(String candidate, int bits) throws Exception {
        if (Generator.leadingZeroBits(Generator.sha256(candidate)) >= bits) {
            // extremely unlikely; pick another counter
            fail("test precondition failed: candidate unexpectedly strong");
        }
    }

    // --- Expiration ---

    @Test
    void expiredChallengeIsRejected() throws Exception {
        int bits = 8;
        Generator g = new Generator(config(STRONG_SECRET, 30, bits), new LocalSecurityStore()); // 30ms TTL
        String challenge = g.token("GET orders");
        String cash = solve(challenge, bits);
        Thread.sleep(120);
        assertFalse(g.validateHashCash(challenge, cash, "GET orders"), "expired challenge must be rejected");
    }

    // --- Malformed input ---

    @Test
    void nullOrBlankInputsAreRejected() throws Exception {
        Generator g = new Generator(config(STRONG_SECRET, 60_000, 8), new LocalSecurityStore());
        assertFalse(g.validateHashCash(null, "x", "r"));
        assertFalse(g.validateHashCash("x", null, "r"));
        assertFalse(g.validateHashCash("", "", "r"));
        assertFalse(g.validateHashCash("not-a-jws", "not-a-jws:1", "r"));
    }

    // --- leadingZeroBits sanity ---

    @Test
    void leadingZeroBitsCountsCorrectly() {
        assertEquals(8, Generator.leadingZeroBits(new byte[]{0x00, (byte) 0xFF}));
        assertEquals(0, Generator.leadingZeroBits(new byte[]{(byte) 0xFF}));
        assertEquals(23, Generator.leadingZeroBits(new byte[]{0x00, 0x00, 0x01}));
        assertEquals(12, Generator.leadingZeroBits(new byte[]{0x00, 0x0F}));
    }
}
