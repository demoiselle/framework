package br.gov.frameworkdemoiselle.internal.context;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.context.RequestContext;

/**
 * Custom request context that stores beans in a thread local store.
 * 
 * @author serpro
 *
 */
@Priority(Priority.MIN_PRIORITY)
public class RequestContextImpl extends AbstractThreadLocalContext implements RequestContext {

	RequestContextImpl() {
		super(RequestScoped.class);
	}

}
