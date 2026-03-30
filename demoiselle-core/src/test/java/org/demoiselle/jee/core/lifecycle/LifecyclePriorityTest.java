/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.lifecycle.model.PriorityLifecycleModelTest;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddBeanClasses({PriorityLifecycleModelTest.class, org.demoiselle.jee.core.message.DemoiselleMessage.class})
@AddExtensions({LifecycleBootstrap.class, org.demoiselle.jee.core.message.MessageBundleExtension.class})
class LifecyclePriorityTest {

    @Inject
    private PriorityLifecycleModelTest model;

    @Inject
    @Destroyed(ApplicationScoped.class)
    private Event<Object> eventDestroyed;

    @Inject
    @Initialized(ApplicationScoped.class)
    private Event<Object> eventInitialized;

    @BeforeEach
    void setUp() {
        this.model.getNumbers().clear();
    }

    @Test
    void methodWithStartupAnnotationsShouldBeExecutated() {

        this.eventInitialized.fire(new Object());

        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);

        assertEquals(numbers, this.model.getNumbers());
    }

    @Test
    void methodWithShutdownAnnotationShouldBeExecutated() {

        this.eventDestroyed.fire(new Object());

        List<Integer> numbers = new ArrayList<>();
        numbers.add(4);
        numbers.add(5);
        numbers.add(6);

        assertEquals(numbers, this.model.getNumbers());
    }

}
