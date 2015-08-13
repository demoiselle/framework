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
package br.gov.frameworkdemoiselle.util;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import br.gov.frameworkdemoiselle.lifecycle.AfterSessionCreated;
import br.gov.frameworkdemoiselle.lifecycle.BeforeSessionDestroyed;

/**
 * <p>
 * Implements the {@link HttpSessionListener} interface and fires two events.
 * </p>
 * <ul>
 * <li><strong>{@link AfterSessionCreated}</strong>: Just after a new HTTP session is created</li>
 * <li><strong>{@link BeforeSessionDestroyed}</strong>: Just before an HTTP session is invalidated</li>
 * </ul>
 * 
 * @author serpro
 */
@WebListener
public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(final HttpSessionEvent sessionEvent) {
		Beans.getBeanManager().fireEvent(new AfterSessionCreated() {

			@Override
			public String getSessionId() {
				HttpSession session = sessionEvent.getSession();
				return session != null ? session.getId() : null;
			}
		});
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent sessionEvent) {
		Beans.getBeanManager().fireEvent(new BeforeSessionDestroyed() {

			@Override
			public String getSessionId() {
				HttpSession session = sessionEvent.getSession();
				return session != null ? session.getId() : null;
			}
		});
	}
}
