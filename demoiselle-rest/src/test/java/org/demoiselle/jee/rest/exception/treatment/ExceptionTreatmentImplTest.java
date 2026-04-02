/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExceptionTreatmentImpl} RFC 9457 mappings.
 *
 * Covers specific exception mappings: ConstraintViolationException, SQLException,
 * and generic exception.
 */
class ExceptionTreatmentImplTest {

    static {
        TestRuntimeDelegate.install();
    }

    private ExceptionTreatmentImpl impl;
    private DemoiselleRestConfig config;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() throws Exception {
        impl = new ExceptionTreatmentImpl();
        config = new DemoiselleRestConfig();
        config.setErrorFormat("rfc9457");
        config.setShowErrorDetails(true);
        setField(impl, "config", config);

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("content-type")).thenReturn(null);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ── ConstraintViolationException ───────────────────────────────

    @Test
    void constraintViolationException_rfc9457_returns412WithViolations() {
        ConstraintViolation<?> violation = new StubConstraintViolation(
                "persist.arg0.name", "must not be null", new User());

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        Response response = impl.getFormatedError(exception, request);

        assertEquals(412, response.getStatus());
        assertEquals("application", response.getMediaType().getType());
        assertEquals("problem+json", response.getMediaType().getSubtype());

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals("Validation Failed", pd.getTitle());
        assertEquals(412, pd.getStatus());
        assertEquals("/api/test", pd.getInstance());
        assertNotNull(pd.getExtensions().get("violations"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> violations = (List<Map<String, String>>) pd.getExtensions().get("violations");
        assertFalse(violations.isEmpty());
        assertEquals("must not be null", violations.get(0).get("message"));
    }

    // ── SQLException ───────────────────────────────────────────────

    @Test
    void sqlException_rfc9457_returns500WithDatabaseError() {
        SQLException exception = new SQLException("unique constraint violated", "23505", 23505);

        Response response = impl.getFormatedError(exception, request);

        assertEquals(500, response.getStatus());

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals("Database Error", pd.getTitle());
        assertEquals(500, pd.getStatus());
        assertEquals("/api/test", pd.getInstance());
        assertEquals("unique constraint violated", pd.getDetail());
    }

    @Test
    void sqlException_rfc9457_hidesDetailWhenShowErrorDetailsFalse() {
        config.setShowErrorDetails(false);
        SQLException exception = new SQLException("secret db info", "42000", 1);

        Response response = impl.getFormatedError(exception, request);

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals("Database Error", pd.getTitle());
        assertEquals(500, pd.getStatus());
        assertNull(pd.getDetail(), "detail must be null when showErrorDetails is false");
    }

    // ── Generic exception ──────────────────────────────────────────

    @Test
    void genericException_rfc9457_returns500WithInternalServerError() {
        RuntimeException exception = new RuntimeException("something went wrong");

        Response response = impl.getFormatedError(exception, request);

        assertEquals(500, response.getStatus());

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals("Internal Server Error", pd.getTitle());
        assertEquals(500, pd.getStatus());
        assertEquals("/api/test", pd.getInstance());
        assertEquals("something went wrong", pd.getDetail());
    }

    @Test
    void genericException_rfc9457_hidesDetailWhenShowErrorDetailsFalse() {
        config.setShowErrorDetails(false);
        RuntimeException exception = new RuntimeException("secret info");

        Response response = impl.getFormatedError(exception, request);

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals("Internal Server Error", pd.getTitle());
        assertEquals(500, pd.getStatus());
        assertNull(pd.getDetail(), "detail must be null when showErrorDetails is false");
    }

    @Test
    void rfc9457_instanceIsNullWhenRequestIsNull() {
        RuntimeException exception = new RuntimeException("error");

        Response response = impl.getFormatedError(exception, null);

        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertNull(pd.getInstance(), "instance must be null when request is null");
    }

    // ── Helper types ───────────────────────────────────────────────

    static class User {}

    /** Simple Path implementation that returns a fixed string. */
    static class SimplePath implements Path {
        private final String value;
        SimplePath(String value) { this.value = value; }
        @Override public String toString() { return value; }
        @Override public Iterator<Node> iterator() { return List.<Node>of().iterator(); }
    }

    /** Stub ConstraintViolation to avoid Mockito issues with Jakarta Validation interfaces on Java 23. */
    @SuppressWarnings("rawtypes")
    static class StubConstraintViolation implements ConstraintViolation {
        private final String propertyPath;
        private final String message;
        private final Object leafBean;

        StubConstraintViolation(String propertyPath, String message, Object leafBean) {
            this.propertyPath = propertyPath;
            this.message = message;
            this.leafBean = leafBean;
        }

        @Override public String getMessage() { return message; }
        @Override public String getMessageTemplate() { return null; }
        @Override public Object getRootBean() { return null; }
        @Override public Class getRootBeanClass() { return null; }
        @Override public Object getLeafBean() { return leafBean; }
        @Override public Object[] getExecutableParameters() { return new Object[0]; }
        @Override public Object getExecutableReturnValue() { return null; }
        @Override public Path getPropertyPath() { return new SimplePath(propertyPath); }
        @Override public Object getInvalidValue() { return null; }
        @Override public ConstraintDescriptor<?> getConstraintDescriptor() { return null; }
        @Override public Object unwrap(Class type) { return null; }
    }
}
