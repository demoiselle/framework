/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest;

import java.util.logging.Logger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * 
 * 
 * @author SERPRO
 *
 */
public class LogExceptionMappers implements Extension {

	private static final Logger logger = Logger.getLogger(LogExceptionMappers.class.getName());

	/**
	 * Process all classes that extends {@link ExceptionMapper} to log for
	 * analysis.
	 * 
	 * @param pat
	 *            ProcessAnnotatedType used by CDI
	 */
	public void processAnnotatedType(@Observes final ProcessAnnotatedType<? extends ExceptionMapper<?>> pat) {
		Class<? extends ExceptionMapper<?>> pcsClass = pat.getAnnotatedType().getJavaClass();
		if (pcsClass.isAnnotationPresent(jakarta.ws.rs.ext.Provider.class)) {
			logger.warning(pcsClass.getCanonicalName());
		}
	}

}
