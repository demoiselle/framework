/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 1: Round-trip de serialização do ProblemDetail

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jqwik.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for ProblemDetail JSON round-trip serialization.
 *
 * <p><b>Validates: Requirements 1.5, 1.6, 1.7</b></p>
 *
 * <ul>
 *   <li>1.5 — Serialize to JSON and deserialize back produces equivalent object</li>
 *   <li>1.6 — Extension fields appear at JSON root level</li>
 *   <li>1.7 — Null fields are omitted from serialized JSON</li>
 * </ul>
 */
class ProblemDetailRoundTripPropertyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> RESERVED_KEYS =
            Set.of("type", "title", "status", "detail", "instance");

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<String> extensionKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(12)
                .filter(k -> !RESERVED_KEYS.contains(k));
    }

    @Provide
    Arbitrary<String> optionalStrings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .injectNull(0.3);
    }

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30);
    }

    @Provide
    Arbitrary<ProblemDetail> arbitraryProblemDetails() {
        Arbitrary<Integer> statuses = Arbitraries.integers().between(100, 599);
        Arbitrary<String> types = Arbitraries.of(
                "about:blank",
                "https://example.com/not-found",
                "urn:problem:custom"
        ).injectNull(0.2);
        Arbitrary<String> titles = optionalStrings();
        Arbitrary<String> details = optionalStrings();
        Arbitrary<String> instances = optionalStrings();

        // Generate 0-3 extension entries with string values (non-reserved keys)
        Arbitrary<Map<String, Object>> extensions = extensionKeys()
                .list().ofMaxSize(3).uniqueElements()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        return Arbitraries.just(Collections.<String, Object>emptyMap());
                    }
                    List<Arbitrary<String>> valueArbs = new ArrayList<>();
                    for (int i = 0; i < keys.size(); i++) {
                        valueArbs.add(nonNullStrings());
                    }
                    return Combinators.combine(valueArbs).as(values -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        for (int i = 0; i < keys.size(); i++) {
                            map.put(keys.get(i), values.get(i));
                        }
                        return map;
                    });
                });

        return Combinators.combine(statuses, types, titles, details, instances, extensions)
                .as((status, type, title, detail, instance, exts) -> {
                    ProblemDetail pd = new ProblemDetail();
                    pd.setStatus(status);
                    if (type != null) {
                        pd.setType(type);
                    }
                    pd.setTitle(title);
                    pd.setDetail(detail);
                    pd.setInstance(instance);
                    exts.forEach(pd::setExtension);
                    return pd;
                });
    }

    // ── Property 1: Round-trip serialization ───────────────────────

    /**
     * For any valid ProblemDetail (status 100-599, type/title/detail/instance
     * optionally null, extensions with non-reserved keys), serializing to JSON
     * and deserializing back must produce an equivalent object.
     *
     * Additionally verifies:
     * - Null fields are omitted in the JSON (Req 1.7)
     * - Extension fields appear at the JSON root level (Req 1.6)
     *
     * <p><b>Validates: Requirements 1.5, 1.6, 1.7</b></p>
     */
    @Property(tries = 100)
    void roundTripSerializationPreservesEquivalence(
            @ForAll("arbitraryProblemDetails") ProblemDetail original
    ) throws JsonProcessingException {

        // ── Serialize ──────────────────────────────────────────────
        String json = MAPPER.writeValueAsString(original);

        // ── Verify null fields are omitted (Req 1.7) ──────────────
        Map<String, Object> rawMap = MAPPER.readValue(json,
                new TypeReference<Map<String, Object>>() {});

        if (original.getTitle() == null) {
            assertFalse(rawMap.containsKey("title"),
                    "JSON must not contain 'title' when it is null");
        }
        if (original.getDetail() == null) {
            assertFalse(rawMap.containsKey("detail"),
                    "JSON must not contain 'detail' when it is null");
        }
        if (original.getInstance() == null) {
            assertFalse(rawMap.containsKey("instance"),
                    "JSON must not contain 'instance' when it is null");
        }

        // ── Verify extensions at root level (Req 1.6) ─────────────
        for (String extKey : original.getExtensions().keySet()) {
            assertTrue(rawMap.containsKey(extKey),
                    "Extension key '" + extKey + "' must appear at JSON root level");
        }

        // ── Deserialize and verify equivalence (Req 1.5) ──────────
        ProblemDetail deserialized = MAPPER.readValue(json, ProblemDetail.class);

        assertEquals(original.getType(), deserialized.getType(),
                "type must survive round-trip");
        assertEquals(original.getTitle(), deserialized.getTitle(),
                "title must survive round-trip");
        assertEquals(original.getStatus(), deserialized.getStatus(),
                "status must survive round-trip");
        assertEquals(original.getDetail(), deserialized.getDetail(),
                "detail must survive round-trip");
        assertEquals(original.getInstance(), deserialized.getInstance(),
                "instance must survive round-trip");
        assertEquals(original.getExtensions(), deserialized.getExtensions(),
                "extensions must survive round-trip");
        assertEquals(original, deserialized,
                "ProblemDetail must be equal after round-trip");
    }
}
