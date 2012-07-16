package br.gov.frameworkdemoiselle.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.gov.frameworkdemoiselle.internal.bootstrap.ShutdownBootstrap;
import br.gov.frameworkdemoiselle.internal.bootstrap.StartupBootstrap;

public class DemoiselleServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		StartupBootstrap.startup();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ShutdownBootstrap.shutdown();
	}
}
