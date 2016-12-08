package org.demoiselle.jee.core.lifecycle;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.demoiselle.jee.core.annotation.Priority;

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
	 * @return Get the current Priority annotated with {@link Priority}
	 */
	protected Integer getPriority(AnnotatedMethod<?> annotatedMethod) {
		Integer priority = Priority.MIN_PRIORITY;

		Priority annotation = annotatedMethod.getAnnotation(Priority.class);
		if (annotation != null) {
			priority = annotation.value();
		}

		return priority;
	}
	
}
