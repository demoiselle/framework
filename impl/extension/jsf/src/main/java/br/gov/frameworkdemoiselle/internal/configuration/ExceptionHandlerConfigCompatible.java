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
package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.Serializable;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.util.Beans;

@Deprecated
@Configuration(prefix = "frameworkdemoiselle.handle")
public class ExceptionHandlerConfigCompatible implements Serializable {

	private static final long serialVersionUID = 1L;

	@Deprecated
	@Name("application.exception")
	private boolean handleApplicationException = true;

	@Deprecated
	@Name("application.exception.page")
	private String exceptionPage = "/application_error";

	@Deprecated
	public boolean isHandleApplicationException() {
		Logger logger = Beans.getReference(Logger.class);
		logger.warn("A propriedade frameworkdemoiselle.handle.application.exception="
				+ handleApplicationException
				+ " não será suportada nas próximas versões do framework. Para evitar futuros problemas atualize a propriedade para frameworkdemoiselle.exception.application.handle="
				+ handleApplicationException);

		return handleApplicationException;
	}

	@Deprecated
	public String getExceptionPage() {
		Logger logger = Beans.getReference(Logger.class);
		logger.warn("A propriedade frameworkdemoiselle.handle.application.exception.page="
				+ exceptionPage
				+ " não será suportada nas próximas versões do framework. Para evitar futuros problemas atualize a propriedade para frameworkdemoiselle.exception.default.redirect.page="
				+ exceptionPage);

		return exceptionPage;
	}
}
