/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.lifecycle.model.PriorityLifecycleModelTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class LifecyclePriorityTest {

    @Inject
    private PriorityLifecycleModelTest model;

    @Inject
    @Destroyed(ApplicationScoped.class)
    private Event<Object> eventDestroyed;

    @Inject
    @Initialized(ApplicationScoped.class)
    private Event<Object> eventInitialized;

    @Before
    public void setUp() {
        this.model.getNumbers().clear();
    }

    @Test
    public void methodWithStartupAnnotationsShouldBeExecutated() {

        this.eventInitialized.fire(new Object());

        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);

        assertEquals(numbers, this.model.getNumbers());
    }

    @Test
    public void methodWithShutdownAnnotationShouldBeExecutated() {

        this.eventDestroyed.fire(new Object());

        List<Integer> numbers = new ArrayList<>();
        numbers.add(4);
        numbers.add(5);
        numbers.add(6);

        assertEquals(numbers, this.model.getNumbers());
    }

}
