package br.gov.frameworkdemoiselle.util;

import javax.servlet.ServletContextEvent;

import br.gov.frameworkdemoiselle.internal.bootstrap.ShutdownBootstrap;
import br.gov.frameworkdemoiselle.internal.bootstrap.StartupBootstrap;

public class ServletContextListener implements javax.servlet.ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		StartupBootstrap.startup();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ShutdownBootstrap.shutdown();
	}
}
