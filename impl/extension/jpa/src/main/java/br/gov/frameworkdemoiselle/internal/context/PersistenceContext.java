package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;


public class PersistenceContext extends AbstractThreadLocalContext {

	public PersistenceContext(Class<? extends Annotation> scope) {
		super(scope);
	}

}
