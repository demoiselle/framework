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
package br.gov.frameworkdemoiselle.exception;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import exception.custom.CustomException;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.stereotype.Controller;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>
 * Intercepts some thrown exceptions, and calls the appropriate method. These interceptor works only in
 * classes annotated with <b>@Contoller</b>. Above we discribe which kind of exception is intercepted and 
 * what is an appropriate method. 
 * <p>
 * To be interceptable, the thrown exception must be from a type which is annotated with 
 * <b>@ApplicationException</b>.
 * <p>
 * An appropriate method must be annotated with <b>@ExceptionHandler</b>, and receive as parameter some 
 * exception that could be thrown for it's class (which have to be annotated with <b>@ApplicationException</b>).
 * So, when this method is called, it's receive the thrown exception as parameter. In the same class shouldn't 
 * be more than one method annotated with <b>@ExceptionHandler</b> and receiving the same type of exception.
 * <p>
 * <p>
 * The examples below shows how these interceptor works:
 * <p>
 * 
 * <blockquote>
 * 
 * <pre>
 * &#064;ApplicationException
 * public class CustomException extends RuntimeException {
 * }
 * 
 * &#064;Controller
 * public class CustomExceptionHandler {
 * 
 * 	public void throwException() {
 *		throw new CustomException();
 *	}
 *	
 *	&#064;ExceptionHandler
 *	public void handler(CustomException exception) {
 *		...
 *	}
 *
 * }
 * </pre>
 * </blockquote>
 * 
 * <p>
 * When the method <b>throwException</b> throw a <b>CustomException</b>, once CustomException is annotated 
 * with @ApplicationException and CustomExceptionHandle is annotated with @Controller, the interceptor will 
 * looking for some method (in CustomExceptionHandle) annotated with @ExceptionHandle and that receive a 
 * CustomException as parameter to call. In the case shown, the method <b>handler</b> is called when a 
 * <b>CustomException</b> is thrown.
 * <p>
 * 
 * @author SERPRO
 */
@Interceptor
@Controller
public class ExceptionHandlerInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private static transient ResourceBundle bundle;

	private static transient Logger logger;

	private final Map<Class<?>, Map<Class<?>, Method>> cache = new HashMap<Class<?>, Map<Class<?>, Method>>();
	
	private boolean handleException(final Exception cause, final Object target) throws Exception {
		getLogger().info(getBundle().getString("handling-exception", cause.getClass().getCanonicalName()));

		boolean handled = false;
		Class<?> type = target.getClass();

		if (!isLoaded(type)) {
			loadHandlers(type);
		}

		Method handler = getMethod(type, cause.getClass());
		if (handler != null) {
			invoke(handler, target, cause);
			handled = true;
		}

		return handled;
	}

	/**
	 * If there is an handler in the current class or superClass for the expected exception, then this method will be
	 * returned; Else returns null;
	 * 
	 * @param type
	 * @param causeClass
	 * @return
	 */
	private Method getMethod(final Class<?> type, final Class<?> causeClass) {
		Method handler = null;
		Map<Class<?>, Method> map = cache.get(type);

		if (map != null && Throwable.class.isAssignableFrom(causeClass)) {
			if (map.containsKey(causeClass)) {
				handler = map.get(causeClass);
			} else {
				handler = getMethod(type, causeClass.getSuperclass());
			}
		}

		return handler;
	}

	/**
	 * Create an map of Exception Handler for this class and put it on the cache.
	 * 
	 * @param type
	 */
	private void loadHandlers(final Class<?> type) {
		Map<Class<?>, Method> mapHandlers = new HashMap<Class<?>, Method>();

		List<Method> methods = new ArrayList<Method>();
		Class<?> tempType = type;

		while (tempType != null) {
			methods.addAll(Arrays.asList(tempType.getMethods()));
			tempType = tempType.getSuperclass();
		}

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
	private void validateHandler(final Method method) {
		if (method.getParameterTypes().length != 1) {
			throw new DemoiselleException(getBundle().getString("must-declare-one-single-parameter",
					method.toGenericString()));
		}
	}

	/**
	 * Indicates if this class is already loaded in cache control.
	 * 
	 * @param type
	 * @return
	 */
	private boolean isLoaded(final Class<?> type) {
		return cache.containsKey(type);
	}

	private void invoke(final Method method, final Object object, final Exception param) throws Exception {
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
		Object target = ic.getTarget();

		try {
			result = ic.proceed();

		} catch (Exception cause) {
			if (!handleException(cause, target)) {
				throw cause;
			}
		}

		return result;
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(ExceptionHandlerInterceptor.class.getName()));
		}

		return logger;
	}
}
