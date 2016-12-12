/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Used to prioritize some execution flow, as methods annotated with @startup or @shutdown,
 * or some interface implementation.
 * </p>
 *
 * @author SERPRO
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
//TODO usar a do java
public @interface Priority {

	/**
	 * Most important priority value.
	 */
	//TODO remover
	static int MAX_PRIORITY = Integer.MIN_VALUE;

	/**
	 * Less important priority value.
	 */
	static int MIN_PRIORITY = Integer.MAX_VALUE;
	
	/**
	 * Less important priority value.
	 */
	static int L1_PRIORITY = MIN_PRIORITY;

	/**
	 * Higher priority than L1_PRIORITY
	 */
	static int L2_PRIORITY = L1_PRIORITY - 100;

	/**
	 * Higher priority than L2_PRIORITY
	 */
	static int L3_PRIORITY = L2_PRIORITY - 100;

	/**
	 * Higher priority than L3_PRIORITY
	 */
	static int L4_PRIORITY = L3_PRIORITY - 100;

	/**
	 * <p>
	 * An integer value defines the priority order. The lower the value, the greater priority.
	 * <p>
	 *
	 * @return Priority value, lower values have higher priority.
	 */
	int value();
}
