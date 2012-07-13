package br.gov.frameworkdemoiselle.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.bootstrap.ShutdownBootstrap;
import br.gov.frameworkdemoiselle.internal.bootstrap.StartupBootstrap;

public class DemoiselleServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			StartupBootstrap.startup();

		} catch (Throwable cause) {
			throw new DemoiselleException(cause);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			ShutdownBootstrap.shutdown();

		} catch (Throwable cause) {
			throw new DemoiselleException(cause);
		}
	}
}
