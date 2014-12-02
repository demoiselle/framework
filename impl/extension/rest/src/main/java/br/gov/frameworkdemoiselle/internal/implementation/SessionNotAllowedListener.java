package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.SessionTrackingMode.URL;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import br.gov.frameworkdemoiselle.internal.configuration.RESTConfig;
import br.gov.frameworkdemoiselle.transaction.BeforeTransactionComplete;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@WebListener
public class SessionNotAllowedListener implements ServletContextListener, HttpSessionListener {

	private static final String ATTR_NAME = "br.gov.frameworkdemoiselle.SESSION_NOT_ALLOWED";

	private static final String ATTR_VALUE = "created";

	private transient RESTConfig config;

	private transient ResourceBundle bundle;

	private transient Logger logger;

	public void contextInitialized(ServletContextEvent event) {
		if (!getConfig().isSessionAllowed()) {
			Set<SessionTrackingMode> modes = new HashSet<SessionTrackingMode>();
			modes.add(URL);
			event.getServletContext().setSessionTrackingModes(modes);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		if (!getConfig().isSessionAllowed()) {
			Beans.getReference(HttpServletRequest.class).setAttribute(ATTR_NAME, ATTR_VALUE);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
	}

	public void beforeTransactionComplete(@Observes BeforeTransactionComplete event, HttpServletRequest request) {
		if (!getConfig().isSessionAllowed() && ATTR_VALUE.equals(request.getAttribute(ATTR_NAME))) {
			invalidateSesstion(request);
			throw new IllegalStateException(getBundle().getString("session-not-allowed"));
		}
	}

	private void invalidateSesstion(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session != null) {
			session.invalidate();
		}
	}

	private RESTConfig getConfig() {
		if (config == null) {
			config = Beans.getReference(RESTConfig.class);
		}

		return config;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-rest-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier("br.gov.frameworkdemoiselle.util"));
		}

		return logger;
	}
}
