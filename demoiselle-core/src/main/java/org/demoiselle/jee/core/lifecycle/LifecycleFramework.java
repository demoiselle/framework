package org.demoiselle.jee.core.lifecycle;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.demoiselle.jee.core.lifecycle.annotation.Shutdown;
import org.demoiselle.jee.core.lifecycle.annotation.Startup;
import org.demoiselle.jee.core.message.DemoiselleMessage;

public class LifecycleFramework {

	@Inject
	private DemoiselleMessage demoiselleMessage;

	private static final Logger logger = Logger.getLogger(LifecycleFramework.class.getName());

	@Startup
	public void start() {
		logger.info("====================================================");
		logger.info(demoiselleMessage.startMessage());
		logger.info(demoiselleMessage.frameworkName() + " " + demoiselleMessage.version());
		logger.info(demoiselleMessage.engineOn());
		logger.info("====================================================");
	}

	/*
	 * Shutdown message only works outside of eclipse.
	 */
	@Shutdown
	public void stop() {
		logger.info("====================================================");
		logger.info(demoiselleMessage.frameworkName() + " " + demoiselleMessage.version());
		logger.info(demoiselleMessage.engineOff());
		logger.info("====================================================");
	}

}
