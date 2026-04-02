/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 3: About:blank preenche title com frase-razão HTTP

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for ProblemDetail about:blank default title behavior.
 *
 * <p><b>Validates: Requirement 1.2</b></p>
 *
 * <ul>
 *   <li>1.2 — When type is "about:blank", ProblemDetail SHALL use the title
 *       corresponding to the HTTP status code reason phrase per RFC 9457 §4.2</li>
 * </ul>
 */
class ProblemDetailAboutBlankPropertyTest {

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<Integer> validStatuses() {
        return Arbitraries.integers().between(100, 599);
    }

    @Provide
    Arbitrary<String> nonAboutBlankTypes() {
        return Arbitraries.of(
                "https://example.com/not-found",
                "urn:problem:custom",
                "https://api.example.org/errors/validation",
                "urn:ietf:rfc:9457"
        );
    }

    @Provide
    Arbitrary<String> nonNullTitles() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30);
    }

    // ── Property 3a: about:blank fills title with reason phrase ────

    /**
     * For any valid HTTP status (100-599), when type is "about:blank" and
     * title is null, applyAboutBlankDefaults() must fill title with the
     * HTTP reason phrase from reasonPhrase(status).
     *
     * <p><b>Validates: Requirement 1.2</b></p>
     */
    @Property(tries = 100)
    void aboutBlankFillsTitleWithReasonPhrase(
            @ForAll("validStatuses") int status
    ) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(status);
        pd.setType("about:blank");
        // title is null by default

        pd.applyAboutBlankDefaults();

        String expectedTitle = ProblemDetail.reasonPhrase(status);
        assertEquals(expectedTitle, pd.getTitle(),
                "When type is 'about:blank' and title is null, applyAboutBlankDefaults() " +
                "must set title to reasonPhrase(" + status + ") = '" + expectedTitle + "'");
    }

    // ── Property 3b: non-about:blank does NOT change title ─────────

    /**
     * When type is NOT "about:blank", applyAboutBlankDefaults() must NOT
     * change the title (it should remain null).
     *
     * <p><b>Validates: Requirement 1.2</b></p>
     */
    @Property(tries = 100)
    void nonAboutBlankDoesNotChangeTitle(
            @ForAll("validStatuses") int status,
            @ForAll("nonAboutBlankTypes") String type
    ) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(status);
        pd.setType(type);
        // title is null by default

        pd.applyAboutBlankDefaults();

        assertNull(pd.getTitle(),
                "When type is '" + type + "' (not about:blank), " +
                "applyAboutBlankDefaults() must NOT change title");
    }

    // ── Property 3c: existing title is NOT overridden ──────────────

    /**
     * When title is already set, applyAboutBlankDefaults() must NOT
     * override it, even when type is "about:blank".
     *
     * <p><b>Validates: Requirement 1.2</b></p>
     */
    @Property(tries = 100)
    void existingTitleIsNotOverridden(
            @ForAll("validStatuses") int status,
            @ForAll("nonNullTitles") String existingTitle
    ) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(status);
        pd.setType("about:blank");
        pd.setTitle(existingTitle);

        pd.applyAboutBlankDefaults();

        assertEquals(existingTitle, pd.getTitle(),
                "When title is already set to '" + existingTitle + "', " +
                "applyAboutBlankDefaults() must NOT override it");
    }
}
