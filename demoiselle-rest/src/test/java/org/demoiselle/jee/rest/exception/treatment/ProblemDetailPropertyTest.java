/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link ProblemDetail}.
 *
 * <p><b>Validates: Requirements 1.1, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4</b></p>
 */
class ProblemDetailPropertyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<Integer> validStatusCodes() {
        return Arbitraries.integers().between(100, 599);
    }

    @Provide
    Arbitrary<String> extensionKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(10)
                .filter(k -> !Set.of("type", "title", "status", "detail", "instance").contains(k));
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
    Arbitrary<ProblemDetail> validProblemDetails() {
        Arbitrary<Integer> statuses = validStatusCodes();
        Arbitrary<String> types = Arbitraries.of(
                "about:blank",
                "https://example.com/not-found",
                "https://example.com/validation",
                "urn:problem:custom"
        );
        Arbitrary<String> titles = optionalStrings();
        Arbitrary<String> details = optionalStrings();
        Arbitrary<String> instances = optionalStrings();

        // Generate 0-3 extension entries with string values
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
                    pd.setType(type);
                    pd.setTitle(title);
                    pd.setDetail(detail);
                    pd.setInstance(instance);
                    exts.forEach(pd::setExtension);
                    return pd;
                });
    }

    // ── Property 1: Round-trip JSON serialization ──────────────────

    // Feature: rest-enhancements, Property 1: Round-trip de serialização JSON do ProblemDetail
    /**
     * For any valid ProblemDetail (status 100-599, standard fields and arbitrary
     * extension fields), serialize to JSON and deserialize back must produce an
     * equivalent object.
     *
     * <p><b>Validates: Requirements 1.1, 1.4, 2.1, 2.3, 2.4</b></p>
     */
    @Property(tries = 100)
    void roundTripJsonSerialization(
            @ForAll("validProblemDetails") ProblemDetail original
    ) throws JsonProcessingException {
        String json = MAPPER.writeValueAsString(original);
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

    // ── Property 2: Null field omission ────────────────────────────

    // Feature: rest-enhancements, Property 2: Omissão de campos nulos na serialização do ProblemDetail
    /**
     * For any ProblemDetail where optional fields (title, detail, instance) are
     * randomly null, the serialized JSON must not contain keys for those null fields.
     *
     * <p><b>Validates: Requirement 2.2</b></p>
     */
    @Property(tries = 100)
    void nullFieldsOmittedFromJson(
            @ForAll("validProblemDetails") ProblemDetail pd
    ) throws JsonProcessingException {
        String json = MAPPER.writeValueAsString(pd);

        if (pd.getTitle() == null) {
            assertFalse(json.contains("\"title\""),
                    "JSON must not contain 'title' key when title is null. JSON: " + json);
        }
        if (pd.getDetail() == null) {
            assertFalse(json.contains("\"detail\""),
                    "JSON must not contain 'detail' key when detail is null. JSON: " + json);
        }
        if (pd.getInstance() == null) {
            assertFalse(json.contains("\"instance\""),
                    "JSON must not contain 'instance' key when instance is null. JSON: " + json);
        }
    }

    // ── Property 3: Status validation ──────────────────────────────

    // Feature: rest-enhancements, Property 3: Validação do campo status do ProblemDetail
    /**
     * For any integer outside 100-599 (and not 0), setting the status field must
     * throw IllegalArgumentException. For any integer within 100-599, the value
     * must be accepted and getStatus() must return it. Status 0 is also accepted
     * (default value).
     *
     * <p><b>Validates: Requirement 1.3</b></p>
     */
    @Property(tries = 200)
    void statusValidation(@ForAll @IntRange(min = -1000, max = 1000) int value) {
        ProblemDetail pd = new ProblemDetail();

        boolean isValid = value == 0 || (value >= 100 && value <= 599);

        if (isValid) {
            pd.setStatus(value);
            assertEquals(value, pd.getStatus(),
                    "getStatus() must return the value that was set: " + value);
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> pd.setStatus(value),
                    "setStatus(" + value + ") must throw IllegalArgumentException");
        }
    }
}
