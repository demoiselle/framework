package br.gov.frameworkdemoiselle.lifecycle;

import javax.servlet.http.HttpSession;

/**
 * This interface represents an event fired after a new HTTP session is created.
 * 
 * @author serpro
 *
 */
public interface AfterSessionCreated {
	
	/**
	 * 
	 * @return The recently created session
	 */
	public HttpSession getSession();

}
