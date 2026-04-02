/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TokenRecord}.
 *
 * <p>Validates: Requirements 4.1, 4.5, 4.6</p>
 */
class TokenRecordTest {

    // --- Construction with null values (Req 4.1) ---

    @Test
    void constructWithNullKeyAndNullType() {
        TokenRecord record = new TokenRecord(null, null);
        assertNull(record.key());
        assertNull(record.type());
        assertNull(record.getKey());
        assertNull(record.getType());
    }

    @Test
    void constructWithNullKey() {
        TokenRecord record = new TokenRecord(null, TokenType.JWT);
        assertNull(record.key());
        assertEquals(TokenType.JWT, record.type());
    }

    @Test
    void constructWithNullType() {
        TokenRecord record = new TokenRecord("abc123", null);
        assertEquals("abc123", record.key());
        assertNull(record.type());
    }

    @Test
    void constructWithValidValues() {
        TokenRecord record = new TokenRecord("myKey", TokenType.BEARER);
        assertEquals("myKey", record.getKey());
        assertEquals(TokenType.BEARER, record.getType());
    }

    // --- Setters throw UnsupportedOperationException (Req 4.5) ---

    @Test
    void setKeyThrowsUnsupportedOperationException() {
        TokenRecord record = new TokenRecord("key", TokenType.JWT);
        assertThrows(UnsupportedOperationException.class, () -> record.setKey("other"));
    }

    @Test
    void setTypeThrowsUnsupportedOperationException() {
        TokenRecord record = new TokenRecord("key", TokenType.JWT);
        assertThrows(UnsupportedOperationException.class, () -> record.setType(TokenType.BASIC));
    }

    // --- Equals based on key only (Req 4.6) ---

    @Test
    void equalsSameKeyDifferentTypes() {
        TokenRecord a = new TokenRecord("token1", TokenType.JWT);
        TokenRecord b = new TokenRecord("token1", TokenType.BEARER);
        assertEquals(a, b);
    }

    @Test
    void equalsSameKeyDifferentTypesSymmetric() {
        TokenRecord a = new TokenRecord("token1", TokenType.BASIC);
        TokenRecord b = new TokenRecord("token1", TokenType.MAC);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void notEqualsDifferentKeys() {
        TokenRecord a = new TokenRecord("key1", TokenType.JWT);
        TokenRecord b = new TokenRecord("key2", TokenType.JWT);
        assertNotEquals(a, b);
    }

    @Test
    void equalsReflexive() {
        TokenRecord record = new TokenRecord("key", TokenType.TOKEN);
        assertEquals(record, record);
    }

    @Test
    void equalsWithNull() {
        TokenRecord record = new TokenRecord("key", TokenType.JWT);
        assertNotEquals(null, record);
    }

    @Test
    void equalsWithNonTokenObject() {
        TokenRecord record = new TokenRecord("key", TokenType.JWT);
        assertNotEquals("not a token", record);
    }

    @Test
    void equalsBothNullKeys() {
        TokenRecord a = new TokenRecord(null, TokenType.JWT);
        TokenRecord b = new TokenRecord(null, TokenType.BEARER);
        assertEquals(a, b);
    }

    @Test
    void hashCodeSameForSameKey() {
        TokenRecord a = new TokenRecord("sameKey", TokenType.JWT);
        TokenRecord b = new TokenRecord("sameKey", TokenType.TOKEN);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeForNullKey() {
        TokenRecord a = new TokenRecord(null, TokenType.JWT);
        TokenRecord b = new TokenRecord(null, TokenType.BASIC);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // --- Cross-type equality with TokenImpl (Req 4.6) ---

    @Test
    void equalsWithTokenImplSameKey() {
        TokenRecord record = new TokenRecord("shared", TokenType.JWT);
        Token impl = new TokenImpl();
        impl.setKey("shared");
        impl.setType(TokenType.BEARER);
        // TokenRecord.equals checks instanceof Token
        assertEquals(record, impl);
    }

    // --- toString (Req 4.1) ---

    @Test
    void toStringContainsKeyAndType() {
        TokenRecord record = new TokenRecord("abc", TokenType.JWT);
        String str = record.toString();
        assertTrue(str.contains("abc"), "toString should contain the key value");
        assertTrue(str.contains("JWT"), "toString should contain the type value");
    }

    @Test
    void toStringWithNulls() {
        TokenRecord record = new TokenRecord(null, null);
        String str = record.toString();
        assertNotNull(str, "toString should not return null even with null fields");
        assertTrue(str.contains("null"), "toString should represent null fields");
    }
}
