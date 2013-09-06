package br.gov.frameworkdemoiselle.internal.context;

import javax.enterprise.context.SessionScoped;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.context.SessionContext;


/**
 * 
 * @author serpro
 *
 */
@Priority(Priority.MIN_PRIORITY)
public class SessionContextImpl extends StaticContextImpl implements SessionContext {

	SessionContextImpl() {
		super(SessionScoped.class);
	}
	
}
