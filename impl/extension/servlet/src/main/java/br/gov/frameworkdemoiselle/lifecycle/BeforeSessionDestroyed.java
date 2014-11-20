package br.gov.frameworkdemoiselle.lifecycle;

import javax.servlet.http.HttpSession;

/**
 * This interface represents an event fired before an HTTP session is destroyed.
 * 
 * @author serpro
 *
 */
public interface BeforeSessionDestroyed {
	
	/**
	 * <p>When calling this method the session still hasn't been invalidated so
	 * you can access attributes that are about to be removed from the session.</p>
	 * 
	 * <p>Don't call {@link HttpSession#invalidate()} on the returned session, this operation
	 * is unsupported.</p>
	 * 
	 * @return The session about to be destroyed
	 */
	public HttpSession getSession();

}