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
package br.gov.frameworkdemoiselle.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Intercepts calls with {@code @RequiredRole} annotations.
 * 
 * @author SERPRO
 */
@Interceptor
@RequiredRole(value = "")
public class RequiredRoleInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private static transient ResourceBundle bundle;

	private static transient Logger logger;

	/**
	 * Gets the value property of {@code @RequiredRole}. Delegates to {@code SecurityContext} check role. If the user
	 * has the required role it executes the mehtod, otherwise throws an exception. Returns what is returned from the
	 * intercepted method. If the method's return type is {@code void} returns {@code null}.
	 * 
	 * @param ic
	 *            the {@code InvocationContext} in which the method is being called
	 * @return what is returned from the intercepted method. If the method's return type is {@code void} returns
	 *         {@code null}
	 * @throws Exception
	 *             if there is an error during the role check or during the method's processing
	 */
	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		List<String> roles = getRoles(ic);

		if (getSecurityContext().isLoggedIn()) {
			getLogger().info(
					getBundle().getString("has-role-verification", getSecurityContext().getUser().getId(),
							roles));
		}

		List<String> userRoles = new ArrayList<String>();

		for (String role : roles) {
			if (getSecurityContext().hasRole(role)) {
				userRoles.add(role);
			}
		}

		if (userRoles.isEmpty()) {
			getLogger()
					.error(getBundle().getString("does-not-have-role", getSecurityContext().getUser().getId(),
							roles));

			throw new AuthorizationException(getBundle().getString("does-not-have-role-ui", roles));
		}

		getLogger().debug(
				getBundle().getString("user-has-role", getSecurityContext().getUser().getId(), userRoles));

		return ic.proceed();
	}

	/**
	 * Returns the value defined in {@code @RequiredRole} annotation.
	 * 
	 * @param ic
	 *            the {@code InvocationContext} in which the method is being called
	 * @return the value defined in {@code @RequiredRole} annotation
	 */
	private List<String> getRoles(InvocationContext ic) {
		String[] roles = {};

		if (ic.getMethod().getAnnotation(RequiredRole.class) == null) {
			if (ic.getTarget().getClass().getAnnotation(RequiredRole.class) != null) {
				roles = ic.getTarget().getClass().getAnnotation(RequiredRole.class).value();
			}
		} else {
			roles = ic.getMethod().getAnnotation(RequiredRole.class).value();
		}

		return Arrays.asList(roles);
	}

	private SecurityContext getSecurityContext() {
		return Beans.getReference(SecurityContext.class);
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(RequiredRoleInterceptor.class.getName()));
		}

		return logger;
	}
}
