/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.pagination;

import java.time.Instant;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.core.pagination.CursorCodec;

/**
 * Additive REST support for cursor (keyset) pagination.
 *
 * <p>
 * This helper reads an opaque {@code cursor} query parameter and produces a
 * {@code next}-page {@code Link} header value. It is intentionally
 * <strong>additive</strong>: it does not interfere with the existing
 * offset/range pagination in the CRUD module — an endpoint may offer both, using
 * the cursor when present and falling back to range otherwise.
 * </p>
 *
 * <p>
 * The cursor is signed/verified with the injected {@link CursorCodec}, so a
 * client cannot forge or tamper with it, and expired cursors are rejected by the
 * codec.
 * </p>
 *
 * @author SERPRO
 */
public final class CursorSupport {

    /** Standard query parameter carrying the opaque cursor token. */
    public static final String CURSOR_PARAM = "cursor";

    private final CursorCodec codec;

    /**
     * @param codec the HMAC cursor codec (server-side secret)
     */
    public CursorSupport(CursorCodec codec) {
        this.codec = codec;
    }

    /**
     * Reads and verifies the {@code cursor} query parameter, if present.
     *
     * @param uriInfo the request URI info
     * @return the decoded {@link Cursor}, or {@code null} when absent
     * @throws CursorCodec.InvalidCursorException if the token is invalid, tampered
     *                                            with or expired
     */
    public Cursor readCursor(UriInfo uriInfo) {
        if (uriInfo == null) {
            return null;
        }
        String token = uriInfo.getQueryParameters().getFirst(CURSOR_PARAM);
        if (token == null || token.isBlank()) {
            return null;
        }
        return codec.decode(token, Instant.now().getEpochSecond());
    }

    /**
     * Encodes the given cursor to an opaque token.
     *
     * @param cursor the cursor to encode
     * @return the signed token
     */
    public String encode(Cursor cursor) {
        return codec.encode(cursor);
    }

    /**
     * Builds an RFC 8288 {@code Link} header value for the next page, preserving
     * all existing query parameters (filters, sort, page size) and replacing only
     * the {@code cursor} parameter — thereby preserving the caller's range/query.
     *
     * @param uriInfo   the request URI info
     * @param nextCursor the cursor pointing at the next page (may be {@code null}
     *                   to indicate no further pages)
     * @return the {@code Link} header value, or {@code null} when there is no next
     *         page
     */
    public String nextLink(UriInfo uriInfo, Cursor nextCursor) {
        if (uriInfo == null || nextCursor == null) {
            return null;
        }
        UriBuilder builder = uriInfo.getRequestUriBuilder()
                .replaceQueryParam(CURSOR_PARAM, encode(nextCursor));
        return "<" + builder.build().toString() + ">; rel=\"next\"";
    }
}
