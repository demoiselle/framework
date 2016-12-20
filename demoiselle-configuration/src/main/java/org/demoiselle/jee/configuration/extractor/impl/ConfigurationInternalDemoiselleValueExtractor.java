package org.demoiselle.jee.configuration.extractor.impl;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(PACKAGE)
public @interface ConfigurationInternalDemoiselleValueExtractor {

}
