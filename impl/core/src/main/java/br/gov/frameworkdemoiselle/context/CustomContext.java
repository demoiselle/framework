package br.gov.frameworkdemoiselle.context;

import javax.enterprise.context.spi.Context;

/**
 * 
 * Base interface for contexts managed by the framework.
 * 
 * @author serpro
 *
 */
public interface CustomContext extends Context {

	/**
	 * Activates a custom context
	 * 
	 * @return <code>true</code> if context was activated, <code>false</code> if there was already another active
	 * context for the same scope and the activation of this scope failed.
	 */
	boolean activate();
	
	/**
	 * Deactivates this context, it will clear all beans stored on this context.
	 */
	void deactivate();
	
}
