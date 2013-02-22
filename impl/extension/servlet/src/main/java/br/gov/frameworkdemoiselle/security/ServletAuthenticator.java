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

import static br.gov.frameworkdemoiselle.internal.implementation.StrategySelector.EXTENSIONS_L1_PRIORITY;

import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.internal.interceptor.TransactionalInterceptor;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Priority(EXTENSIONS_L1_PRIORITY)
public class ServletAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private static ResourceBundle bundle;

	private static Logger logger;

	@Override
	public boolean authenticate() {
		boolean result;

		try {
			getRequest().login(getCredentials().getUsername(), getCredentials().getPassword());
			result = true;

		} catch (ServletException cause) {
			getLogger().debug(getBundle().getString(cause.getLocalizedMessage()));

			result = false;
		}

		return result;
	}

	@Override
	public void unAuthenticate() {
		getCredentials().clear();
		getRequest().getSession().invalidate();
	}

	@Override
	public User getUser() {
		User user = null;
		final Principal userPincipal = getRequest().getUserPrincipal();

		if (userPincipal != null) {
			user = new User() {

				private static final long serialVersionUID = 1L;

				@Override
				public String getId() {
					return userPincipal.getName();
				}

				@Override
				public void setAttribute(Object key, Object value) {
				}

				@Override
				public Object getAttribute(Object key) {
					return null;
				}
			};
		}

		return user;
	}

	protected Credentials getCredentials() {
		return Beans.getReference(Credentials.class);
	}

	private HttpServletRequest getRequest() {
		return Beans.getReference(HttpServletRequest.class);
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-servlet-bundle");
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(TransactionalInterceptor.class);
		}

		return logger;
	}
}
