/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle.model;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.demoiselle.jee.core.lifecycle.annotation.DemoiselleLifecyclePriority;
import org.demoiselle.jee.core.lifecycle.annotation.Shutdown;
import org.demoiselle.jee.core.lifecycle.annotation.Startup;

@ApplicationScoped
public class PriorityLifecycleModelTest {

    private List<Integer> numbers = new ArrayList<>();

    @Startup
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_1)
    public void startupMethod1() {
        this.numbers.add(1);
    }

    @Startup
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_2)
    public void startupMethod2() {
        this.numbers.add(2);
    }

    @Startup
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_3)
    public void startupMethod3() {
        this.numbers.add(3);
    }

    @Startup
    public void startupMethod4() {
        this.numbers.add(4);
    }

    @Shutdown
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_1)
    public void shutdownMethod1() {
        this.numbers.add(4);
    }

    @Shutdown
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_2)
    public void shutdownMethod2() {
        this.numbers.add(5);
    }

    @Shutdown
    @DemoiselleLifecyclePriority(DemoiselleLifecyclePriority.LEVEL_3)
    public void shutdownMethod3() {
        this.numbers.add(6);
    }

    public List<Integer> getNumbers() {
        return this.numbers;
    }

}
