/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 5: Múltiplas mensagens incluídas como extensão

import java.lang.reflect.Field;
import java.util.List;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test verifying that multiple messages in a
 * DemoiselleRestException are included as a "messages" extension
 * in the resulting ProblemDetail.
 *
 * <p><b>Validates: Requirements 2.4</b></p>
 */
class ExceptionMultipleMessagesPropertyTest {

    static {
        TestRuntimeDelegate.install();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ExceptionTreatmentImpl createInstance() throws Exception {
        ExceptionTreatmentImpl impl = new ExceptionTreatmentImpl();

        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat("rfc9457");
        config.setShowErrorDetails(true);
        setField(impl, "config", config);

        DemoiselleRESTMessage messages = mock(DemoiselleRESTMessage.class);
        setField(impl, "messages", messages);

        return impl;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<List<DemoiselleRestExceptionMessage>> multipleMessages() {
        Arbitrary<DemoiselleRestExceptionMessage> singleMessage =
                Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30).injectNull(0.3),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).injectNull(0.3)
                ).as(DemoiselleRestExceptionMessage::new);

        return singleMessage.list().ofMinSize(2).ofMaxSize(10);
    }

    // ── Property 5: Multiple messages as extension ─────────────────

    /**
     * For any DemoiselleRestException with 2+ messages, the ProblemDetail
     * must contain a "messages" extension whose size equals the number
     * of messages in the exception.
     *
     * <p><b>Validates: Requirements 2.4</b></p>
     */
    @Property(tries = 100)
    void multipleMessagesIncludedAsExtension(
            @ForAll("multipleMessages") List<DemoiselleRestExceptionMessage> msgs,
            @ForAll @IntRange(min = 100, max = 599) int statusCode
    ) throws Exception {
        // Build exception with 2+ messages
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        for (DemoiselleRestExceptionMessage msg : msgs) {
            exception.addMessage(msg.error(), msg.errorDescription(), msg.errorLink());
        }

        // Map to ProblemDetail
        ExceptionTreatmentImpl impl = createInstance();
        ProblemDetail pd = impl.mapToProblemDetail(exception, true);

        // Verify "messages" extension exists
        Object messagesExt = pd.getExtensions().get("messages");
        assertNotNull(messagesExt,
                "ProblemDetail must contain 'messages' extension when exception has 2+ messages");

        // Verify it is a List with size equal to the number of messages
        assertInstanceOf(List.class, messagesExt,
                "'messages' extension must be a List");

        @SuppressWarnings("unchecked")
        List<?> messagesList = (List<?>) messagesExt;

        // DemoiselleRestException uses a HashSet internally, so duplicates
        // may be collapsed. The property holds against the actual stored size.
        int expectedSize = exception.getMessages().size();
        assertEquals(expectedSize, messagesList.size(),
                "Size of 'messages' extension must equal the number of messages in the exception");
    }
}
