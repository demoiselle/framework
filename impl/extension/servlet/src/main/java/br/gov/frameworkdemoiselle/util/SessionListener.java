package br.gov.frameworkdemoiselle.util;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import br.gov.frameworkdemoiselle.lifecycle.BeforeSessionDestroyed;
import br.gov.frameworkdemoiselle.lifecycle.AfterSessionCreated;

/**
 * <p>Implements the {@link HttpSessionListener} interface and fires two events.</p>
 * 
 * <ul>
 * <li><strong>{@link AfterSessionCreated}</strong>: Just after a new HTTP session is created</li>
 * <li><strong>{@link BeforeSessionDestroyed}</strong>: Just before an HTTP session is invalidated</li>
 * </ul>
 * 
 * @author serpro
 *
 */
public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(final HttpSessionEvent sessionEvent) {
		Beans.getBeanManager().fireEvent(new AfterSessionCreated() {
			@Override
			public String getSessionId() {
				HttpSession session = sessionEvent.getSession();
				return session != null ? session.getId() : null;
			}
		});
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent sessionEvent) {
		Beans.getBeanManager().fireEvent(new BeforeSessionDestroyed() {
			@Override
			public String getSessionId() {
				HttpSession session = sessionEvent.getSession();
				return session != null ? session.getId() : null;
			}
		});
	}
}
