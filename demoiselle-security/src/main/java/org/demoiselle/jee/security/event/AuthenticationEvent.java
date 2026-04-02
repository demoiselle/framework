/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.event;

import java.time.Instant;

import org.demoiselle.jee.core.api.security.DemoiselleUser;

/**
 * CDI event fired for authentication actions (login, logout, failure).
 *
 * @author Demoiselle Framework
 */
public record AuthenticationEvent(
    DemoiselleUser user,
    Action action,
    Instant timestamp
) {

    public enum Action { LOGIN, LOGOUT, FAILURE }

    /**
     * Convenience constructor that uses the current instant as timestamp.
     */
    public AuthenticationEvent(DemoiselleUser user, Action action) {
        this(user, action, Instant.now());
    }
}
