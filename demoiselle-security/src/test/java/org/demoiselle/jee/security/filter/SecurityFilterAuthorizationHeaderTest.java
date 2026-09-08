/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.impl.TokenImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityFilterAuthorizationHeaderTest {

    @Test
    void absentOrBlankHeaderProducesNoToken() {
        assertNull(SecurityFilter.parseAuthorizationHeader(null));
        assertNull(SecurityFilter.parseAuthorizationHeader("   "));
    }

    @Test
    void bearerSchemeIsCaseInsensitiveAndWhitespaceIsNormalized() {
        TokenImpl token = SecurityFilter.parseAuthorizationHeader("  bearer   abc.def.ghi  ");

        assertEquals(TokenType.BEARER, token.getType());
        assertEquals("abc.def.ghi", token.getKey());
    }

    @Test
    void missingCredentialsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SecurityFilter.parseAuthorizationHeader("Bearer"));
    }

    @Test
    void unsupportedSchemeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SecurityFilter.parseAuthorizationHeader("Digest value"));
    }

    @Test
    void multipleCredentialsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SecurityFilter.parseAuthorizationHeader("Bearer first Basic second"));
    }
}
