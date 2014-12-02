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
package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.SessionTrackingMode.URL;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import br.gov.frameworkdemoiselle.internal.configuration.RESTConfig;
import br.gov.frameworkdemoiselle.transaction.BeforeTransactionComplete;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@WebListener
public class SessionNotAllowedListener implements ServletContextListener, HttpSessionListener {

	private static final String ATTR_NAME = "br.gov.frameworkdemoiselle.SESSION_NOT_ALLOWED";

	private static final String ATTR_VALUE = "created";

	private transient RESTConfig config;

	private transient ResourceBundle bundle;

	public void contextInitialized(ServletContextEvent event) {
		if (!getConfig().isSessionAllowed()) {
			Set<SessionTrackingMode> modes = new HashSet<SessionTrackingMode>();
			modes.add(URL);
			event.getServletContext().setSessionTrackingModes(modes);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		if (!getConfig().isSessionAllowed()) {
			Beans.getReference(HttpServletRequest.class).setAttribute(ATTR_NAME, ATTR_VALUE);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
	}

	public void beforeTransactionComplete(@Observes BeforeTransactionComplete event, HttpServletRequest request) {
		if (!getConfig().isSessionAllowed() && ATTR_VALUE.equals(request.getAttribute(ATTR_NAME))) {
			invalidateSesstion(request);
			throw new IllegalStateException(getBundle().getString("session-not-allowed"));
		}
	}

	private void invalidateSesstion(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session != null) {
			session.invalidate();
		}
	}

	private RESTConfig getConfig() {
		if (config == null) {
			config = Beans.getReference(RESTConfig.class);
		}

		return config;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-rest-bundle"));
		}

		return bundle;
	}
}
