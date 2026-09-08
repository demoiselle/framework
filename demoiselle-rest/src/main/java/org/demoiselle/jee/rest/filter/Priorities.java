/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

/**
 * Priority constants for Demoiselle REST filters, layered relative to the
 * standard {@link jakarta.ws.rs.Priorities} values.
 *
 * @author SERPRO
 */
final class Priorities {

    private Priorities() {
    }

    /**
     * Idempotency runs early (before user filters) on the request side so it can
     * short-circuit replays, and correspondingly late on the response side.
     */
    static final int IDEMPOTENCY = jakarta.ws.rs.Priorities.AUTHORIZATION + 100;

    /**
     * Deprecation/lifecycle headers are decorative and run at the header
     * decorator stage, like the other Demoiselle response filters.
     */
    static final int DEPRECATION = jakarta.ws.rs.Priorities.HEADER_DECORATOR;
}
