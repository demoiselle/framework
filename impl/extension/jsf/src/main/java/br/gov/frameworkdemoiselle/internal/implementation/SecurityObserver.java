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

import br.gov.frameworkdemoiselle.internal.configuration.JsfSecurityConfig;
import br.gov.frameworkdemoiselle.security.AfterLoginSuccessful;
import br.gov.frameworkdemoiselle.security.AfterLogoutSuccessful;
import br.gov.frameworkdemoiselle.util.PageNotFoundException;
import br.gov.frameworkdemoiselle.util.Redirector;

import com.sun.faces.config.ConfigurationException;

@SessionScoped
public class SecurityObserver implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FacesContext facesContext;

	@Inject
	private JsfSecurityConfig config;

	@Inject
	private HttpSession session;

	private Map<String, Object> savedParams = new HashMap<String, Object>();

	private String savedViewId;

	@Inject
	private Logger logger;

	public SecurityObserver() {
		clear();
	}

	private void saveCurrentState() {
		clear();

		if (!config.getLoginPage().equals(facesContext.getViewRoot().getViewId())) {
			savedParams.putAll(facesContext.getExternalContext().getRequestParameterMap());
			savedViewId = facesContext.getViewRoot().getViewId();
		}
	}

	public void redirectToLoginPage() {
		saveCurrentState();

		try {
			Redirector.redirect(config.getLoginPage());

		} catch (PageNotFoundException cause) {
			// TODO Colocar a mensagem no bundle
			throw new ConfigurationException(
					"A tela de login \""
							+ cause.getViewId()
							+ "\" não foi encontrada. Caso o seu projeto possua outra, defina no arquivo de configuração a chave \""
							+ "frameworkdemoiselle.security.login.page" + "\"", cause);
		}
	}

	public void onLoginSuccessful(@Observes final AfterLoginSuccessful event) {
		boolean redirectedFromConfig = false;

		try {
			if (savedViewId != null) {
				Redirector.redirect(savedViewId, savedParams);

			} else if (config.isRedirectEnabled()) {
				redirectedFromConfig = true;
				Redirector.redirect(config.getRedirectAfterLogin(), savedParams);
			}

		} catch (PageNotFoundException cause) {
			if (redirectedFromConfig) {
				// TODO Colocar a mensagem no bundle
				throw new ConfigurationException(
						"A tela \""
								+ cause.getViewId()
								+ "\" que é invocada após o logon não foi encontrada. Caso o seu projeto possua outra, defina no arquivo de configuração a chave \""
								+ "frameworkdemoiselle.security.redirect.after.login" + "\"", cause);
			} else {
				throw cause;
			}

		} finally {
			clear();
		}
	}

	public void onLogoutSuccessful(@Observes final AfterLogoutSuccessful event) {
		try {
			if (config.isRedirectEnabled()) {
				Redirector.redirect(config.getRedirectAfterLogout());
			}

		} catch (PageNotFoundException cause) {
			// TODO Colocar a mensagem no bundle
			throw new ConfigurationException(
					"A tela \""
							+ cause.getViewId()
							+ "\" que é invocada após o logout não foi encontrada. Caso o seu projeto possua outra, defina no arquivo de configuração a chave \""
							+ "frameworkdemoiselle.security.redirect.after.logout" + "\"", cause);

		} finally {
			try {
				session.invalidate();
			} catch (IllegalStateException e) {
				logger.debug("Esta sessão já foi invalidada.");
			}
		}
	}

	private void clear() {
		savedViewId = null;
		savedParams.clear();
	}
}
