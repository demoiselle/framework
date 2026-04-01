/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link DemoiselleRestExceptionMessage} record.
 *
 * Validates: Requirements 2.2, 2.3, 2.4
 */
class DemoiselleRestExceptionMessageTest {

    // --- Construction tests ---

    @Test
    void validConstructionWithAllFields() {
        var msg = new DemoiselleRestExceptionMessage("NOT_FOUND", "Resource not found", "https://docs.example.com/404");
        assertEquals("NOT_FOUND", msg.error());
        assertEquals("Resource not found", msg.errorDescription());
        assertEquals("https://docs.example.com/404", msg.errorLink());
    }

    @Test
    void validConstructionWithNullOptionalFields() {
        var msg = new DemoiselleRestExceptionMessage("BAD_REQUEST", null, null);
        assertEquals("BAD_REQUEST", msg.error());
        assertNull(msg.errorDescription());
        assertNull(msg.errorLink());
    }

    // --- Null rejection (Requirement 2.2) ---

    @Test
    void nullErrorShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new DemoiselleRestExceptionMessage(null, "desc", "link"));
    }

    // --- equals / hashCode (Requirement 2.4) ---

    @Test
    void recordsWithSameValuesShouldBeEqual() {
        var a = new DemoiselleRestExceptionMessage("ERR", "desc", "link");
        var b = new DemoiselleRestExceptionMessage("ERR", "desc", "link");
        assertEquals(a, b);
    }

    @Test
    void recordsWithSameValuesShouldHaveSameHashCode() {
        var a = new DemoiselleRestExceptionMessage("ERR", "desc", "link");
        var b = new DemoiselleRestExceptionMessage("ERR", "desc", "link");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void recordsWithDifferentValuesShouldNotBeEqual() {
        var a = new DemoiselleRestExceptionMessage("ERR1", "desc", null);
        var b = new DemoiselleRestExceptionMessage("ERR2", "desc", null);
        assertNotEquals(a, b);
    }

    // --- JSON round-trip with Jackson (Requirement 2.3) ---

    @Test
    void jsonRoundTripWithAllFields() throws Exception {
        var mapper = new ObjectMapper();
        var original = new DemoiselleRestExceptionMessage("FORBIDDEN", "Access denied", "https://docs.example.com/403");

        String json = mapper.writeValueAsString(original);
        var deserialized = mapper.readValue(json, DemoiselleRestExceptionMessage.class);

        assertEquals(original, deserialized);
    }

    @Test
    void jsonRoundTripWithNullOptionalFields() throws Exception {
        var mapper = new ObjectMapper();
        var original = new DemoiselleRestExceptionMessage("INTERNAL", null, null);

        String json = mapper.writeValueAsString(original);
        var deserialized = mapper.readValue(json, DemoiselleRestExceptionMessage.class);

        assertEquals(original, deserialized);
    }

    @Test
    void jsonSerializationProducesValidJson() throws Exception {
        var mapper = new ObjectMapper();
        var msg = new DemoiselleRestExceptionMessage("ERR", "description", "http://link");

        String json = mapper.writeValueAsString(msg);

        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("\"errorDescription\""));
        assertTrue(json.contains("\"errorLink\""));
    }
}
