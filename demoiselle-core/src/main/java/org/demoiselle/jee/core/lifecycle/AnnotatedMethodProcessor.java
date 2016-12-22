/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.demoiselle.jee.core.lifecycle.annotation.DemoiselleLifecyclePriority;

/**
 * Represents an annotated method to be processed 
 * 
 * @author SERPRO
 */
public class AnnotatedMethodProcessor {

	private AnnotatedMethod<?> annotatedMethod;
	
	public AnnotatedMethodProcessor(final AnnotatedMethod<?> annotatedMethod) {
		this.annotatedMethod = annotatedMethod;
	}
	
	protected AnnotatedMethod<?> getAnnotatedMethod(){
		return this.annotatedMethod;
	}
	
	/**
	 * 
	 * @param annotatedMethod Represents the method
	 * @return Get the current Priority annotated with {@link DemoiselleLifecyclePriority}
	 */
	protected Integer getPriority(AnnotatedMethod<?> annotatedMethod) {
		Integer priority = DemoiselleLifecyclePriority.LEVEL_4;

		DemoiselleLifecyclePriority annotation = annotatedMethod.getAnnotation(DemoiselleLifecyclePriority.class);
		if (annotation != null) {
			priority = annotation.value();
		}

		return priority;
	}
	
}
