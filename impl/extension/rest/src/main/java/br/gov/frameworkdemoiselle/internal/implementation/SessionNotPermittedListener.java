package br.gov.frameworkdemoiselle.internal.implementation;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class SessionNotPermittedListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		event.getSession().invalidate();
		throw new IllegalStateException("Session use is not permitted.");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
	}
}
