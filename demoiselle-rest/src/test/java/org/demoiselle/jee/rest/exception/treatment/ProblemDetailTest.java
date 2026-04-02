/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProblemDetail} covering default values, edge cases,
 * equals/hashCode, toString, and extension key validation.
 */
class ProblemDetailTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Default values ─────────────────────────────────────────────

    @Test
    void defaultTypeShouldBeAboutBlank() {
        ProblemDetail pd = new ProblemDetail();
        assertEquals("about:blank", pd.getType());
    }

    @Test
    void defaultStatusShouldBeZero() {
        ProblemDetail pd = new ProblemDetail();
        assertEquals(0, pd.getStatus());
    }

    @Test
    void defaultNullableFieldsShouldBeNull() {
        ProblemDetail pd = new ProblemDetail();
        assertNull(pd.getTitle());
        assertNull(pd.getDetail());
        assertNull(pd.getInstance());
    }

    @Test
    void defaultExtensionsShouldBeEmpty() {
        ProblemDetail pd = new ProblemDetail();
        assertTrue(pd.getExtensions().isEmpty());
    }

    // ── Status validation edge cases ───────────────────────────────

    @Test
    void setStatusAcceptsZero() {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(0);
        assertEquals(0, pd.getStatus());
    }

    @Test
    void setStatusAcceptsBoundary100() {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(100);
        assertEquals(100, pd.getStatus());
    }

    @Test
    void setStatusAcceptsBoundary599() {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(599);
        assertEquals(599, pd.getStatus());
    }

    @Test
    void setStatusRejects99() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class, () -> pd.setStatus(99));
    }

    @Test
    void setStatusRejects600() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class, () -> pd.setStatus(600));
    }

    @Test
    void setStatusRejectsNegative() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class, () -> pd.setStatus(-1));
    }

    // ── Extension key validation ───────────────────────────────────

    @Test
    void setExtensionRejectsReservedKeyType() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension("type", "value"));
    }

    @Test
    void setExtensionRejectsReservedKeyTitle() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension("title", "value"));
    }

    @Test
    void setExtensionRejectsReservedKeyStatus() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension("status", 404));
    }

    @Test
    void setExtensionRejectsReservedKeyDetail() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension("detail", "value"));
    }

    @Test
    void setExtensionRejectsReservedKeyInstance() {
        ProblemDetail pd = new ProblemDetail();
        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension("instance", "value"));
    }

    @Test
    void setExtensionAcceptsCustomKey() {
        ProblemDetail pd = new ProblemDetail();
        pd.setExtension("traceId", "abc-123");
        assertEquals("abc-123", pd.getExtensions().get("traceId"));
    }

    @Test
    void extensionBuilderStyleChaining() {
        ProblemDetail pd = new ProblemDetail();
        ProblemDetail result = pd.extension("a", 1).extension("b", 2);
        assertSame(pd, result);
        assertEquals(Map.of("a", 1, "b", 2), pd.getExtensions());
    }

    @Test
    void extensionOverwritesPreviousValue() {
        ProblemDetail pd = new ProblemDetail();
        pd.setExtension("key", "old");
        pd.setExtension("key", "new");
        assertEquals("new", pd.getExtensions().get("key"));
        assertEquals(1, pd.getExtensions().size());
    }

    // ── equals / hashCode ──────────────────────────────────────────

    @Test
    void equalsWithSameValues() {
        ProblemDetail a = createSample();
        ProblemDetail b = createSample();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsReflexive() {
        ProblemDetail pd = createSample();
        assertEquals(pd, pd);
    }

    @Test
    void notEqualToNull() {
        ProblemDetail pd = createSample();
        assertNotEquals(null, pd);
    }

    @Test
    void notEqualToDifferentType() {
        ProblemDetail pd = createSample();
        assertNotEquals("string", pd);
    }

    @Test
    void notEqualWhenStatusDiffers() {
        ProblemDetail a = createSample();
        ProblemDetail b = createSample();
        b.setStatus(500);
        assertNotEquals(a, b);
    }

    @Test
    void notEqualWhenExtensionsDiffer() {
        ProblemDetail a = createSample();
        ProblemDetail b = createSample();
        b.setExtension("extra", "value");
        assertNotEquals(a, b);
    }

    // ── toString ───────────────────────────────────────────────────

    @Test
    void toStringContainsAllFields() {
        ProblemDetail pd = createSample();
        String str = pd.toString();
        assertTrue(str.contains("ProblemDetail{"));
        assertTrue(str.contains("type='about:blank'"));
        assertTrue(str.contains("title='Not Found'"));
        assertTrue(str.contains("status=404"));
        assertTrue(str.contains("detail='Resource not found'"));
        assertTrue(str.contains("instance='/api/items/42'"));
        assertTrue(str.contains("extensions="));
    }

    // ── JSON serialization edge cases ──────────────────────────────

    @Test
    void jsonOmitsNullFields() throws JsonProcessingException {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(500);
        String json = MAPPER.writeValueAsString(pd);

        assertTrue(json.contains("\"type\""));
        assertTrue(json.contains("\"status\""));
        assertFalse(json.contains("\"title\""));
        assertFalse(json.contains("\"detail\""));
        assertFalse(json.contains("\"instance\""));
    }

    @Test
    void jsonIncludesExtensionsAtRootLevel() throws JsonProcessingException {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(400);
        pd.setExtension("traceId", "abc-123");

        String json = MAPPER.writeValueAsString(pd);
        assertTrue(json.contains("\"traceId\""));
        assertTrue(json.contains("\"abc-123\""));
        // extensions should NOT appear as a nested object
        assertFalse(json.contains("\"extensions\""));
    }

    @Test
    void jsonRoundTripWithExtensions() throws JsonProcessingException {
        ProblemDetail original = createSample();
        original.setExtension("traceId", "xyz");

        String json = MAPPER.writeValueAsString(original);
        ProblemDetail deserialized = MAPPER.readValue(json, ProblemDetail.class);

        assertEquals(original, deserialized);
    }

    // ── reasonPhrase ──────────────────────────────────────────────

    @Test
    void reasonPhraseReturnsNotFoundFor404() {
        assertEquals("Not Found", ProblemDetail.reasonPhrase(404));
    }

    @Test
    void reasonPhraseReturnsOKFor200() {
        assertEquals("OK", ProblemDetail.reasonPhrase(200));
    }

    @Test
    void reasonPhraseReturnsInternalServerErrorFor500() {
        assertEquals("Internal Server Error", ProblemDetail.reasonPhrase(500));
    }

    @Test
    void reasonPhraseReturnsUnknownStatusForUnrecognizedCode() {
        assertEquals("Unknown Status", ProblemDetail.reasonPhrase(999));
    }

    // ── applyAboutBlankDefaults ────────────────────────────────────

    @Test
    void applyAboutBlankDefaultsFillsTitleWhenAboutBlankAndTitleNull() {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(404);
        // type defaults to "about:blank", title defaults to null
        pd.applyAboutBlankDefaults();
        assertEquals("Not Found", pd.getTitle());
    }

    @Test
    void applyAboutBlankDefaultsDoesNotOverrideExistingTitle() {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(404);
        pd.setTitle("Custom Title");
        pd.applyAboutBlankDefaults();
        assertEquals("Custom Title", pd.getTitle());
    }

    @Test
    void applyAboutBlankDefaultsDoesNothingWhenTypeIsNotAboutBlank() {
        ProblemDetail pd = new ProblemDetail();
        pd.setType("urn:example:custom");
        pd.setStatus(404);
        pd.applyAboutBlankDefaults();
        assertNull(pd.getTitle());
    }

    @Test
    void applyAboutBlankDefaultsDoesNothingWhenStatusIsZero() {
        ProblemDetail pd = new ProblemDetail();
        // type is "about:blank", title is null, status is 0
        pd.applyAboutBlankDefaults();
        assertNull(pd.getTitle());
    }

    // ── Helper ─────────────────────────────────────────────────────

    private ProblemDetail createSample() {
        ProblemDetail pd = new ProblemDetail();
        pd.setType("about:blank");
        pd.setTitle("Not Found");
        pd.setStatus(404);
        pd.setDetail("Resource not found");
        pd.setInstance("/api/items/42");
        return pd;
    }
}
