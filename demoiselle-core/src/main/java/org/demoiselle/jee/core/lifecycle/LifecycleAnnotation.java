package org.demoiselle.jee.core.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface LifecycleAnnotation {

}
