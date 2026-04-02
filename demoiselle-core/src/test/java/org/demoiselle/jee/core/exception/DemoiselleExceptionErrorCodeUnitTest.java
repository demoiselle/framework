/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DemoiselleException errorCode feature.
 *
 * Verifies that existing constructors keep errorCode null and that
 * subclasses inherit the errorCode field and getErrorCode() method.
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3, 3.5, 3.7, 3.10</b></p>
 */
class DemoiselleExceptionErrorCodeUnitTest {

    // -----------------------------------------------------------------------
    // Existing constructors keep errorCode null
    // -----------------------------------------------------------------------

    @Test
    void defaultConstructor_errorCodeIsNull() {
        DemoiselleException ex = new DemoiselleException();
        assertNull(ex.getErrorCode(), "Default constructor should leave errorCode null");
    }

    @Test
    void messageConstructor_errorCodeIsNull() {
        DemoiselleException ex = new DemoiselleException("some error");
        assertNull(ex.getErrorCode(), "Message constructor should leave errorCode null");
        assertEquals("some error", ex.getMessage());
    }

    @Test
    void causeConstructor_errorCodeIsNull() {
        Throwable cause = new RuntimeException("root cause");
        DemoiselleException ex = new DemoiselleException(cause);
        assertNull(ex.getErrorCode(), "Cause constructor should leave errorCode null");
        assertSame(cause, ex.getCause());
    }

    @Test
    void messageAndCauseConstructor_errorCodeIsNull() {
        Throwable cause = new RuntimeException("root cause");
        DemoiselleException ex = new DemoiselleException("some error", cause);
        assertNull(ex.getErrorCode(), "Message+cause constructor should leave errorCode null");
        assertEquals("some error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    // -----------------------------------------------------------------------
    // Subclass inheritance: DemoiselleLifecycleException
    // -----------------------------------------------------------------------

    @Test
    void lifecycleException_inheritsErrorCodeField() {
        DemoiselleLifecycleException ex = new DemoiselleLifecycleException(new Exception("lifecycle error"));
        assertNull(ex.getErrorCode(), "DemoiselleLifecycleException should inherit errorCode as null");
        assertInstanceOf(DemoiselleException.class, ex);
    }

    @Test
    void lifecycleException_getErrorCodeMethodIsAccessible() {
        DemoiselleLifecycleException ex = new DemoiselleLifecycleException(new Exception("test"));
        // getErrorCode() is inherited from DemoiselleException
        assertDoesNotThrow(ex::getErrorCode,
                "getErrorCode() should be accessible on DemoiselleLifecycleException");
    }

    // -----------------------------------------------------------------------
    // Subclass inheritance via anonymous subclass (simulates other subclasses)
    // -----------------------------------------------------------------------

    @Test
    void anonymousSubclass_inheritsErrorCodeAsNull() {
        DemoiselleException subclass = new DemoiselleException("subclass error") {};
        assertNull(subclass.getErrorCode(),
                "Anonymous subclass should inherit errorCode as null from message constructor");
    }

    @Test
    void anonymousSubclass_canAccessErrorCodeViaInheritance() {
        // Simulates how DemoiselleConfigurationException, DemoiselleRestException,
        // DemoiselleScriptException inherit errorCode without redefinition
        DemoiselleException subclass = new DemoiselleException("test", "DEMOISELLE-CFG-001") {};
        assertEquals("DEMOISELLE-CFG-001", subclass.getErrorCode(),
                "Subclass should inherit errorCode set via parent constructor");
    }
}
