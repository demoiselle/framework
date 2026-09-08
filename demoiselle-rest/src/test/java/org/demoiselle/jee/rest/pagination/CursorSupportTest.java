/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.core.pagination.CursorCodec;
import org.demoiselle.jee.core.pagination.CursorCodec.InvalidCursorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CursorSupport}.
 *
 * <p>Validates Requirement (5): additive REST cursor read/write, tamper
 * rejection, and range/query preservation in the next Link.</p>
 */
class CursorSupportTest {

    private final CursorCodec codec = new CursorCodec("rest-cursor-secret");
    private final CursorSupport support = new CursorSupport(codec);

    private static Cursor sample() {
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("id", "10");
        return new Cursor(keys, Cursor.Direction.AFTER, 0);
    }

    @Test
    @DisplayName("readCursor returns null when no cursor param present")
    void readNoCursor() {
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        when(uriInfo.getQueryParameters()).thenReturn(params);
        assertNull(support.readCursor(uriInfo));
    }

    @Test
    @DisplayName("readCursor decodes a valid cursor token")
    void readValidCursor() {
        String token = codec.encode(sample());
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle(CursorSupport.CURSOR_PARAM, token);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Cursor decoded = support.readCursor(uriInfo);
        assertEquals(sample(), decoded);
    }

    @Test
    @DisplayName("readCursor rejects a tampered token")
    void readTamperedCursor() {
        String token = codec.encode(sample());
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle(CursorSupport.CURSOR_PARAM, tampered);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        assertThrows(InvalidCursorException.class, () -> support.readCursor(uriInfo));
    }

    @Test
    @DisplayName("nextLink preserves existing query params and only replaces the cursor")
    void nextLinkPreservesRange() {
        UriInfo uriInfo = mock(UriInfo.class);
        FakeUriBuilder builder = new FakeUriBuilder(
                URI.create("http://host/api/users?page=2&size=20&cursor=abc.def"));
        when(uriInfo.getRequestUriBuilder()).thenReturn(builder);

        String link = support.nextLink(uriInfo, sample());
        assertTrue(link.contains("page=2"), "existing range/query param preserved");
        assertTrue(link.contains("size=20"), "existing range/query param preserved");
        assertTrue(link.endsWith("rel=\"next\""));
    }

    @Test
    @DisplayName("nextLink returns null when there is no next cursor")
    void nextLinkNullWhenNoCursor() {
        UriInfo uriInfo = mock(UriInfo.class);
        assertNull(support.nextLink(uriInfo, null));
    }

    /**
     * Minimal concrete {@link UriBuilder} supporting only the two operations the
     * helper uses: {@code replaceQueryParam(cursor, ...)} and {@code build()}.
     * Java 25 + Mockito cannot mock the abstract {@code UriBuilder}, so we hand-roll
     * a fake that preserves the base query params and swaps only the cursor.
     */
    static final class FakeUriBuilder extends UriBuilder {
        private URI uri;

        FakeUriBuilder(URI uri) {
            this.uri = uri;
        }

        @Override
        public UriBuilder replaceQueryParam(String name, Object... values) {
            String base = uri.toString();
            String path = base.contains("?") ? base.substring(0, base.indexOf('?')) : base;
            String query = base.contains("?") ? base.substring(base.indexOf('?') + 1) : "";
            StringBuilder rebuilt = new StringBuilder();
            for (String pair : query.split("&")) {
                if (pair.isEmpty()) continue;
                String key = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
                if (key.equals(name)) continue; // drop the param to replace
                if (rebuilt.length() > 0) rebuilt.append('&');
                rebuilt.append(pair);
            }
            if (values != null && values.length > 0) {
                if (rebuilt.length() > 0) rebuilt.append('&');
                rebuilt.append(name).append('=').append(values[0]);
            }
            this.uri = URI.create(path + (rebuilt.length() > 0 ? "?" + rebuilt : ""));
            return this;
        }

        @Override
        public URI build(Object... values) {
            return uri;
        }

        // ── unused abstract operations ──
        @Override public UriBuilder clone() { return new FakeUriBuilder(uri); }
        @Override public UriBuilder uri(URI uri) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder uri(String uriTemplate) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder scheme(String scheme) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder schemeSpecificPart(String ssp) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder userInfo(String ui) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder host(String host) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder port(int port) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder replacePath(String path) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder path(String path) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder path(Class resource) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder path(Class resource, String method) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder path(java.lang.reflect.Method method) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder segment(String... segments) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder replaceMatrix(String matrix) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder matrixParam(String name, Object... values) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder replaceMatrixParam(String name, Object... values) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder replaceQuery(String query) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder queryParam(String name, Object... values) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder fragment(String fragment) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplate(String name, Object value) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplateFromEncoded(String name, Object value) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplates(java.util.Map<String, Object> templateValues) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplates(java.util.Map<String, Object> templateValues, boolean encodeSlashInPath) { throw new UnsupportedOperationException(); }
        @Override public UriBuilder resolveTemplatesFromEncoded(java.util.Map<String, Object> templateValues) { throw new UnsupportedOperationException(); }
        @Override public String toTemplate() { throw new UnsupportedOperationException(); }
        @Override public URI buildFromMap(java.util.Map<String, ?> values) { throw new UnsupportedOperationException(); }
        @Override public URI buildFromMap(java.util.Map<String, ?> values, boolean encodeSlashInPath) { throw new UnsupportedOperationException(); }
        @Override public URI buildFromEncodedMap(java.util.Map<String, ?> values) { throw new UnsupportedOperationException(); }
        @Override public URI build(Object[] values, boolean encodeSlashInPath) { return uri; }
        @Override public URI buildFromEncoded(Object... values) { return uri; }
    }
}
