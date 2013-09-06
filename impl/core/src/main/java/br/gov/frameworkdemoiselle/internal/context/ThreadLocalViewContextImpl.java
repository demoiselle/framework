package br.gov.frameworkdemoiselle.internal.context;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.context.ViewContext;

@Priority(Priority.MIN_PRIORITY)
public class ThreadLocalViewContextImpl extends AbstractThreadLocalContext implements ViewContext {

	ThreadLocalViewContextImpl() {
		super(ViewScoped.class);
	}

}
