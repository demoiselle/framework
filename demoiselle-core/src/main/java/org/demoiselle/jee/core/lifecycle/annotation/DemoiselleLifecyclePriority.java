/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to prioritize methods annotated with {@link Startup} or {@link Shutdown}
 *
 * @author SERPRO
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface DemoiselleLifecyclePriority {

    /**
     * The higher priority
     */
	final int LEVEL_1 = 100;

	/**
	 * Second higher priority
	 */
	final int LEVEL_2 = LEVEL_1 + 100;

	/**
	 * Third higher priority
	 */
	final int LEVEL_3 = LEVEL_2 + 100;

	/**
	 * The lower priority
	 */
	final int LEVEL_4 = LEVEL_3 + 100;

	/**
	 * <p>
	 * An integer value defines the priority order. The lower the value, the greater priority.
	 * <p>
	 *
	 * @return Priority value, lower values have higher priority.
	 */
	int value();
}
