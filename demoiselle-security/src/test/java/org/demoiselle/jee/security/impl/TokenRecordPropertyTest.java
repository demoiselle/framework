/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.TokenType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for {@link TokenRecord} immutability.
 *
 * <p><b>Validates: Requirements 4.5</b></p>
 */
class TokenRecordPropertyTest {

    @Provide
    Arbitrary<String> tokenKeys() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(100).injectNull(0.1);
    }

    @Provide
    Arbitrary<TokenType> tokenTypes() {
        return Arbitraries.of(TokenType.class).injectNull(0.1);
    }

    // Feature: security-enhancements, Property 8: TokenRecord setters lançam UnsupportedOperationException
    /**
     * Property 8: For any instance of TokenRecord, invoking setKey(String) or
     * setType(TokenType) must throw UnsupportedOperationException, guaranteeing
     * the record's immutability.
     *
     * <p><b>Validates: Requirements 4.5</b></p>
     */
    @Property(tries = 100)
    void settersThrowUnsupportedOperation(
            @ForAll("tokenKeys") String key,
            @ForAll("tokenTypes") TokenType type,
            @ForAll("tokenKeys") String newKey,
            @ForAll("tokenTypes") TokenType newType) {

        TokenRecord record = new TokenRecord(key, type);

        assertThrows(UnsupportedOperationException.class,
                () -> record.setKey(newKey),
                "setKey() must throw UnsupportedOperationException on TokenRecord");

        assertThrows(UnsupportedOperationException.class,
                () -> record.setType(newType),
                "setType() must throw UnsupportedOperationException on TokenRecord");

        // Verify the record's state was not modified
        assertEquals(key, record.getKey(),
                "key must remain unchanged after setKey() throws");
        assertEquals(type, record.getType(),
                "type must remain unchanged after setType() throws");
    }

    // Feature: security-enhancements, Property 9: TokenRecord igualdade baseada no campo key
    /**
     * Property 9: For any two TokenRecord with the same key value (regardless of type),
     * equals() must return true and hashCode() must return the same value.
     * For TokenRecord with different key values, equals() must return false.
     *
     * <p><b>Validates: Requirements 4.6</b></p>
     */
    @Property(tries = 100)
    void equalityBasedOnKey(
            @ForAll("tokenKeys") String key,
            @ForAll("tokenTypes") TokenType type1,
            @ForAll("tokenTypes") TokenType type2,
            @ForAll("tokenKeys") String differentKey) {

        TokenRecord record1 = new TokenRecord(key, type1);
        TokenRecord record2 = new TokenRecord(key, type2);

        // Same key → equals must return true
        assertEquals(record1, record2,
                "TokenRecords with the same key must be equal regardless of type");

        // Same key → hashCode must be the same
        assertEquals(record1.hashCode(), record2.hashCode(),
                "TokenRecords with the same key must have the same hashCode");

        // Reflexive: a record must equal itself
        assertEquals(record1, record1,
                "TokenRecord must be equal to itself");

        // Different key → equals must return false (when keys actually differ)
        Assume.that(differentKey == null ? key != null : !differentKey.equals(key));
        TokenRecord record3 = new TokenRecord(differentKey, type1);
        assertNotEquals(record1, record3,
                "TokenRecords with different keys must not be equal");
    }
}
