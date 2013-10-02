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

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.configuration.ExceptionHandlerConfigCompatible;
import br.gov.frameworkdemoiselle.internal.configuration.ExceptionHandlerConfig;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Exceptions;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.PageNotFoundException;
import br.gov.frameworkdemoiselle.util.Redirector;

@SuppressWarnings("deprecation")
public class ApplicationExceptionHandler extends AbstractExceptionHandler {

	public ApplicationExceptionHandler(final ExceptionHandler wrapped) {
		super(wrapped);
	}

	protected boolean handleException(final Throwable cause, FacesContext facesContext) {
		// Apenas para manter compatibilidade entre 2.3.x e 2.4.0-RCx
		ExceptionHandlerConfigCompatible compatibleConfig = Beans.getReference(ExceptionHandlerConfigCompatible.class);
		// Usuário está utilizando pelo menos uma das propriedades com a forma depreciada de forma explícita
		if (!(compatibleConfig.getExceptionPage().equals("/application_error") && compatibleConfig.isHandleApplicationException())) {
			Logger logger = Beans.getReference(Logger.class);
			logger.warn("As propriedades frameworkdemoiselle.handle.application.exception e"
					+ " frameworkdemoiselle.handle.application.exception.page"
					+ " não serão suportadas nas próximas versões do framework."
					+ " Para evitar futuros problemas atualize as propriedades para"
					+ " frameworkdemoiselle.exception.application.handle e"
					+ " frameworkdemoiselle.exception.default.redirect.page, respectivamente.");
			return handleExceptionCompatibleConfiguration(compatibleConfig, cause, facesContext);
		}
		
		boolean handled = false;
		ExceptionHandlerConfig config = Beans.getReference(ExceptionHandlerConfig.class);

		if (config.isApplicationExceptionHandle() && Exceptions.isApplicationException(cause)) {

			if (isRendering(facesContext)) {
				handled = handlingDuringRenderResponse(cause, config);
			} else {
				Faces.addMessage(cause);
				handled = true;
			}
		}

		return handled;
	}

	@Deprecated
	private boolean handleExceptionCompatibleConfiguration(ExceptionHandlerConfigCompatible config, final Throwable cause,
			FacesContext facesContext) {
		boolean handled = false;

		if (config.isHandleApplicationException() && Exceptions.isApplicationException(cause)) {

			if (isRendering(facesContext)) {
				handled = handlingDuringRenderResponseCompatible(cause, config);
			} else {
				Faces.addMessage(cause);
				handled = true;
			}
		}
		return handled;
	}

	@Deprecated
	private boolean handlingDuringRenderResponseCompatible(final Throwable cause, final ExceptionHandlerConfigCompatible config) {
		boolean handled = false;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("exception", cause.getMessage());
			Redirector.redirect(config.getExceptionPage(), map);
			handled = true;
		} catch (PageNotFoundException ex) {
			// TODO Colocar a mensagem no bundle
			throw new DemoiselleException(
					"A tela de exibição de erros: \""
							+ ex.getViewId()
							+ "\" não foi encontrada. Caso o seu projeto possua outra, defina no arquivo de configuração a chave \""
							+ "frameworkdemoiselle.handle.application.exception.page" + "\"", ex);
		}
		return handled;
	}

	private boolean isRendering(FacesContext context) {
		return PhaseId.RENDER_RESPONSE.equals(context.getCurrentPhaseId());
	}

	/**
	 * In render response phase an exception interrupt the renderization. So this method will redirect the renderingo to
	 * an page configured in demoiselle.properties
	 * 
	 * @see ExceptionHandlerConfigCompatible
	 * @param cause
	 * @param config
	 * @return
	 */
	private boolean handlingDuringRenderResponse(final Throwable cause, final ExceptionHandlerConfig config) {
		boolean handled = false;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("exception", cause.getMessage());
			Redirector.redirect(config.getDefaultRedirectExceptionPage(), map);
			handled = true;
		} catch (PageNotFoundException ex) {
			// TODO Colocar a mensagem no bundle
			throw new DemoiselleException(
					"A tela de exibição de erros: \""
							+ ex.getViewId()
							+ "\" não foi encontrada. Caso o seu projeto possua outra, defina no arquivo de configuração a chave \""
							+ "frameworkdemoiselle.handle.application.exception.page" + "\"", ex);
		}
		return handled;
	}

	protected Throwable getRoot(final Throwable throwable) {
		Throwable root = throwable;

		while (root != null && !Exceptions.isApplicationException(root)) {
			root = root.getCause();
		}

		return root;
	}
}
