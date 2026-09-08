/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class DemoiselleJwtValidatorTest {

    @Test
    void missingTokenIsRejected() {
        JwtValidationResult result = new DemoiselleJwtValidator().validate(" ");

        assertFalse(result.valid());
        assertEquals("Missing JWT token", result.detail());
    }

    @Test
    void absentBeanManagerOrJwtModuleFailsClosed() {
        JwtValidationResult result = new DemoiselleJwtValidator().validate("header.payload.signature");

        assertFalse(result.valid());
        assertEquals("JWT validator unavailable", result.detail());
    }
}
