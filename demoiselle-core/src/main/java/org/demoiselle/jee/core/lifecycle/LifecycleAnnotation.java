/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate other annotations with this one to
 * mark them as lifecycle annotations, meaning
 * the lifecycle processor of the framework will
 * read them and fire events based on their represented
 * lifecycle stages.
 *
 * @author SERPRO
 */
@Inherited
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface LifecycleAnnotation {

}
