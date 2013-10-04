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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.JsfSecurityConfig;
import br.gov.frameworkdemoiselle.security.AfterLoginSuccessful;
import br.gov.frameworkdemoiselle.security.AfterLogoutSuccessful;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.PageNotFoundException;
import br.gov.frameworkdemoiselle.util.Redirector;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@SessionScoped
public class SecurityObserver implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient JsfSecurityConfig config;

	private transient Map<String, Object> savedParams;

	private String savedViewId;

	@Inject
	private Logger logger;
	
	@Inject
	@Name("demoiselle-jsf-bundle")
	private ResourceBundle bundle;

	public SecurityObserver() {
		clear();
	}

	private Map<String, Object> getSavedParams() {
		if (this.savedParams == null) {
			this.savedParams = new HashMap<String, Object>();
		}

		return this.savedParams;
	}

	public JsfSecurityConfig getConfig() {
		if (this.config == null) {
			this.config = Beans.getReference(JsfSecurityConfig.class);
		}

		return this.config;
	}

	private void saveCurrentState() {
		clear();
		FacesContext facesContext = Beans.getReference(FacesContext.class);

		if (!getConfig().getLoginPage().equals(facesContext.getViewRoot().getViewId())) {
			getSavedParams().putAll(facesContext.getExternalContext().getRequestParameterMap());
			savedViewId = facesContext.getViewRoot().getViewId();
		}
	}

	public void redirectToLoginPage() {
		saveCurrentState();

		try {
			Redirector.redirect(getConfig().getLoginPage());

		} catch (PageNotFoundException cause) {
			throw new ConfigurationException( bundle.getString("login-page-not-found",cause.getViewId()) , cause);
		}
	}

	public void onLoginSuccessful(@Observes final AfterLoginSuccessful event) {
		boolean redirectedFromConfig = false;

		try {
			if (savedViewId != null) {
				Redirector.redirect(savedViewId, getSavedParams());

			} else if (getConfig().isRedirectEnabled()) {
				redirectedFromConfig = true;
				Redirector.redirect(getConfig().getRedirectAfterLogin(), getSavedParams());
			}

		} catch (PageNotFoundException cause) {
			if (redirectedFromConfig) {
				throw new ConfigurationException( bundle.getString("after-login-page-not-found",cause.getViewId()) , cause);
			} else {
				throw cause;
			}

		} finally {
			clear();
		}
	}

	public void onLogoutSuccessful(@Observes final AfterLogoutSuccessful event) {
		try {
			if (getConfig().isRedirectEnabled()) {
				Redirector.redirect(getConfig().getRedirectAfterLogout());
			}

		} catch (PageNotFoundException cause) {
			throw new ConfigurationException( bundle.getString("after-logout-page-not-found",cause.getViewId()) , cause);

		} finally {
			try {
				HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
				session.invalidate();
			} catch (IllegalStateException e) {
				logger.debug("Esta sessão já foi invalidada.");
			}
		}
	}

	private void clear() {
		savedViewId = null;
		getSavedParams().clear();
	}

}
