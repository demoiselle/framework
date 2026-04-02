/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.event;

import java.time.Instant;
import java.util.List;

import org.demoiselle.jee.core.api.security.DemoiselleUser;

/**
 * CDI event fired when an authorization check fails.
 *
 * @author Demoiselle Framework
 */
public record AuthorizationEvent(
    DemoiselleUser user,
    String resource,
    String operation,
    List<String> requiredRoles,
    Instant timestamp
) {

    /**
     * Compact constructor that defensively copies the requiredRoles list.
     */
    public AuthorizationEvent {
        requiredRoles = requiredRoles == null ? List.of() : List.copyOf(requiredRoles);
    }

    /**
     * Convenience constructor that uses the current instant as timestamp.
     */
    public AuthorizationEvent(DemoiselleUser user, String resource, String operation,
                              List<String> requiredRoles) {
        this(user, resource, operation, requiredRoles, Instant.now());
    }
}
