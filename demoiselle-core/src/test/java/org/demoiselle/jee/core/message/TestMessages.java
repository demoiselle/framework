/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;

/**
 * Test @MessageBundle interface for integration tests.
 */
@MessageBundle
public interface TestMessages {

    @MessageTemplate("{hello}")
    String hello();

    @MessageTemplate("{greeting}")
    String greeting(String name);

    @MessageTemplate("{missing-key}")
    String missingKey();

    /**
     * Method without @MessageTemplate — should return null.
     */
    String noTemplate();
}
