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

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.security.RequiredPermission;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * Intercepts calls with {@code @RequiredPermission} annotations.
 * 
 * @author SERPRO
 */
@Interceptor
@RequiredPermission
public class RequiredPermissionInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Instance<SecurityContext> securityContext;

	private final ResourceBundle bundle;

	private final Logger logger;

	@Inject
	public RequiredPermissionInterceptor(Instance<SecurityContext> securityContext,
			@Name("demoiselle-core-bundle") ResourceBundle bundle, Logger logger) {
		this.securityContext = securityContext;
		this.bundle = bundle;
		this.logger = logger;
	}

	/**
	 * Gets the values for both resource and operation properties of {@code @RequiredPermission}. Delegates to
	 * {@code SecurityContext} check permissions. If the user has the required permission it executes the mehtod,
	 * otherwise throws an exception. Returns what is returned from the intercepted method. If the method's return type
	 * is {@code void} returns {@code null}.
	 * 
	 * @param ic
	 *            the {@code InvocationContext} in which the method is being called
	 * @return what is returned from the intercepted method. If the method's return type is {@code void} returns
	 *         {@code null}
	 * @throws Exception
	 *             if there is an error during the permission check or during the method's processing
	 */
	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		String resource = getResource(ic);
		String operation = getOperation(ic);
		String username = null;

		if (securityContext.get().isLoggedIn()) {
			username = getUsername();
			logger.trace(bundle.getString("access-checking", username, operation, resource));
		}

		if (!securityContext.get().hasPermission(resource, operation)) {
			logger.error(bundle.getString("access-denied", username, operation, resource));
			throw new AuthorizationException(bundle.getString("access-denied-ui", resource, operation));
		}

		logger.debug(bundle.getString("access-allowed", username, operation, resource));
		return ic.proceed();
	}

	/**
	 * Returns the id of the currently logged in user.
	 * 
	 * @return the id of the currently logged in user
	 */
	private String getUsername() {
		String username = "";
		User user = securityContext.get().getUser();

		if (user != null && user.getId() != null) {
			username = user.getId();
		}

		return username;
	}

	/**
	 * Returns the resource defined in {@code @RequiredPermission} annotation, the name defined in {@code @Name}
	 * annotation or the class name itself
	 * 
	 * @param ic
	 *            the {@code InvocationContext} in which the method is being called
	 * @return the resource defined in {@code @RequiredPermission} annotation, the name defined in {@code @Name}
	 *         annotation or the class name itself
	 */
	private String getResource(InvocationContext ic) {
		RequiredPermission requiredPermission = ic.getMethod().getAnnotation(RequiredPermission.class);

		if (requiredPermission == null || Strings.isEmpty(requiredPermission.resource())) {
			if (ic.getTarget().getClass().getAnnotation(Name.class) == null) {
				return ic.getTarget().getClass().getSimpleName();
			} else {
				return ic.getTarget().getClass().getAnnotation(Name.class).value();
			}
		} else {
			return requiredPermission.resource();
		}
	}

	/**
	 * Returns the operation defined in {@code @RequiredPermission} annotation, the name defined in {@code @Name}
	 * annotation or the method's name itself
	 * 
	 * @param ic
	 *            the {@code InvocationContext} in which the method is being called
	 * @return the operation defined in {@code @RequiredPermission} annotation, the name defined in {@code @Name}
	 *         annotation or the method's name itself
	 */
	private String getOperation(InvocationContext ic) {
		RequiredPermission requiredPermission = ic.getMethod().getAnnotation(RequiredPermission.class);

		if (requiredPermission == null || Strings.isEmpty(requiredPermission.operation())) {
			if (ic.getMethod().getAnnotation(Name.class) == null) {
				return ic.getMethod().getName();
			} else {
				return ic.getMethod().getAnnotation(Name.class).value();
			}
		} else {
			return requiredPermission.operation();
		}
	}
}
