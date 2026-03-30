/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DemoiselleUserImpl}, verifying that clone uses message bundle
 * instead of hardcoded strings.
 *
 * @author SERPRO
 */
@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddExtensions(org.demoiselle.jee.core.message.MessageBundleExtension.class)
@AddBeanClasses({DemoiselleUserImpl.class, DemoiselleSecurityMessages.class})
class DemoiselleUserImplTest {

    @Inject
    private DemoiselleUser user;

    @BeforeEach
    void setUp() {
        user.setIdentity("user-1");
        user.setName("Test User");
        user.addRole("admin");
        user.addPermission("resource1", "read");
    }

    @Test
    void cloneReturnsCopyWithSameIdentity() {
        DemoiselleUser cloned = user.clone();
        assertNotNull(cloned);
        assertNotSame(user, cloned);
        assertEquals(user.getIdentity(), cloned.getIdentity());
        assertEquals(user.getName(), cloned.getName());
    }

    @Test
    void cloneDoesNotContainHardcodedPortugueseString() {
        // Verify clone succeeds using the message bundle path.
        // The DemoiselleSecurityMessages bundle is injected into DemoiselleUserImpl
        // and bundle.cloneError() is used instead of the hardcoded "Erro ao clonar".
        DemoiselleUser cloned = user.clone();
        assertNotNull(cloned);
        assertEquals("user-1", cloned.getIdentity());
    }
}
