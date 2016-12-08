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

import org.demoiselle.jee.core.annotation.Priority;
import org.demoiselle.jee.core.lifecycle.annotation.Shutdown;
import org.demoiselle.jee.core.lifecycle.annotation.Startup;

@ApplicationScoped
public class PriorityLifecycleModelTest {
	
	private List<Integer> numbers = new ArrayList<>();
		
	@Startup
	@Priority(Priority.L4_PRIORITY)
	public void startupMethod1(){
		this.numbers.add(1);
	}
	
	@Startup
	@Priority(Priority.L3_PRIORITY)
	public void startupMethod2(){		
		this.numbers.add(2);
	}
	
	@Startup
	@Priority(Priority.L2_PRIORITY)
	public void startupMethod3(){
		this.numbers.add(3);
	}
	
	@Startup
	public void startupMethod4(){
		this.numbers.add(4);
	}
	
	@Shutdown
	@Priority(Priority.L4_PRIORITY)
	public void shutdownMethod1(){
		this.numbers.add(4);
	}
	
	@Shutdown
	@Priority(Priority.L3_PRIORITY)
	public void shutdownMethod2(){		
		this.numbers.add(5);
	}
	
	@Shutdown
	@Priority(Priority.L2_PRIORITY)
	public void shutdownMethod3(){
		this.numbers.add(6);
	}
	
	public List<Integer> getNumbers(){
		return this.numbers;
	}

}
