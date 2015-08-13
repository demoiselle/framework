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

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import br.gov.frameworkdemoiselle.lifecycle.BeforeRequestDestroyed;
import br.gov.frameworkdemoiselle.lifecycle.BeforeRequestInitialized;

/**
 * <p>
 * Implements the {@link javax.servlet.ServletRequestListener} interface and fires two events.
 * </p>
 * <ul>
 * <li><strong>{@link BeforeRequestInitialized}</strong>: Just before a new HTTP request comes into scope</li>
 * <li><strong>{@link BeforeRequestDestroyed}</strong>: Just before an HTTP request will go out of scope</li>
 * </ul>
 * 
 * @author serpro
 */
@WebListener
public class RequestListener implements ServletRequestListener {

	@Override
	public void requestDestroyed(final ServletRequestEvent sre) {
		Beans.getBeanManager().fireEvent(new BeforeRequestDestroyed() {

			@Override
			public ServletRequest getRequest() {
				return sre.getServletRequest();
			}

			@Override
			public ServletContext getServletContext() {
				return sre.getServletContext();
			}
		});
	}

	@Override
	public void requestInitialized(final ServletRequestEvent sre) {
		Beans.getBeanManager().fireEvent(new BeforeRequestDestroyed() {

			@Override
			public ServletRequest getRequest() {
				return sre.getServletRequest();
			}

			@Override
			public ServletContext getServletContext() {
				return sre.getServletContext();
			}
		});
	}

}
