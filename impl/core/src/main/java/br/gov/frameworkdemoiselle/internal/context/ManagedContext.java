package br.gov.frameworkdemoiselle.internal.context;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.stereotype.ManagementController;

/**
 * Context that stores {@link RequestScoped} beans during client calls to {@link ManagementController} classes.
 * This context is only activated when no other context is active for {@link RequestScoped}.
 * 
 * @author serpro
 *
 */
public class ManagedContext extends ThreadLocalContext {
	
	/**
	 * Constructs a new context.
	 */
	public ManagedContext() {
		super(RequestScoped.class);
	}

}
