/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.interceptor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.stereotype.Controller;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Interceptor
@Controller
public class ExceptionHandlerInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final ResourceBundle bundle;

	private final Logger logger;

	@Inject
	public ExceptionHandlerInterceptor(Logger logger, @Name("demoiselle-core-bundle") ResourceBundle bundle) {
		this.logger = logger;
		this.bundle = bundle;
	}

	private final Map<Class<?>, Map<Class<?>, Method>> cache = new HashMap<Class<?>, Map<Class<?>, Method>>();

	private final boolean handleException(final Exception cause, final InvocationContext ic) throws Exception {
		logger.info(bundle.getString("handling-exception", cause.getClass().getCanonicalName()));

		boolean handled = false;
		Class<?> type = getType(ic);

		if (!isLoaded(type)) {
			loadHandlers(type);
		}

		Method handler = getMethod(type, cause);
		if (handler != null) {
			invoke(handler, ic.getTarget(), cause);
			handled = true;
		}

		return handled;
	}

	private final Class<?> getType(final InvocationContext ic) {
		Class<?> type = ic.getTarget().getClass();

		if (!CoreBootstrap.isAnnotatedType(type)) {
			type = type.getSuperclass();
			logger.debug(bundle.getString("proxy-detected", ic.getTarget().getClass(), ic.getTarget().getClass()
					.getSuperclass()));
		}

		return type;
	}

	/**
	 * If there is an handler in the current class for the expected exception, then this method will be returned; Else
	 * returns null;
	 * 
	 * @param type
	 * @param cause
	 * @return
	 */
	private final Method getMethod(final Class<?> type, final Exception cause) {
		Method handler = null;

		if (cache.containsKey(type) && cache.get(type).containsKey(cause.getClass())) {
			handler = cache.get(type).get(cause.getClass());
		}

		return handler;
	}

	/**
	 * Create an map of Exception Handler for this class and put it on the cache.
	 * 
	 * @param type
	 */
	private final void loadHandlers(final Class<?> type) {
		Map<Class<?>, Method> mapHandlers = new HashMap<Class<?>, Method>();
		Method[] methods = type.getMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(ExceptionHandler.class)) {
				validateHandler(method);
				mapHandlers.put(method.getParameterTypes()[0], method);
			}
		}
		cache.put(type, mapHandlers);
	}

	/**
	 * Verify the method for compliance with an handler. It must be: public, single parameter, parameter type must be
	 * assigned from Exception
	 * 
	 * @param method
	 */
	private final void validateHandler(final Method method) {
		if (method.getParameterTypes().length != 1) {
			throw new DemoiselleException(bundle.getString("must-declare-one-single-parameter",
					method.toGenericString()));
		}
	}

	/**
	 * Indicates if this class is already loaded in cache control.
	 * 
	 * @param type
	 * @return
	 */
	private final boolean isLoaded(final Class<?> type) {
		return cache.containsKey(type);
	}

	private final void invoke(final Method method, final Object object, final Exception param) throws Exception {
		boolean accessible = method.isAccessible();
		method.setAccessible(true);

		try {
			method.invoke(object, param);
		} catch (InvocationTargetException cause) {
			Throwable targetTrowable = cause.getTargetException();
			if (targetTrowable instanceof Exception) {
				throw (Exception) targetTrowable;
			} else {
				throw new Exception(targetTrowable);
			}
		}

		method.setAccessible(accessible);
	}

	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		Object result = null;

		try {
			result = ic.proceed();
		} catch (Exception cause) {
			if (!handleException(cause, ic)) {
				throw cause;
			}
		}

		return result;
	}

}
