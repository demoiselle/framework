package org.demoiselle.jee.rest;

import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * 
 * 
 * @author SERPRO
 *
 */
public class LogMappers implements Extension {

	private static final Logger logger = Logger.getLogger(LogMappers.class.getName());

	/**
	 * Process all classes that extends {@link ExceptionMapper} to log for
	 * analysis.
	 * 
	 * @param pat
	 *            ProcessAnnotatedType used by CDI
	 */
	public void processAnnotatedType(@Observes final ProcessAnnotatedType<? extends ExceptionMapper<?>> pat) {
		Class<? extends ExceptionMapper<?>> pcsClass = pat.getAnnotatedType().getJavaClass();
		if (pcsClass.isAnnotationPresent(javax.ws.rs.ext.Provider.class)) {
			logger.warning(pcsClass.getCanonicalName());
		}
	}

}
