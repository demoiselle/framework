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

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import br.gov.frameworkdemoiselle.NotFoundException;
import br.gov.frameworkdemoiselle.ServiceUnavailableException;
import br.gov.frameworkdemoiselle.util.Metadata;

@Path("metadata")
public class MetadataREST {

	@Inject
	private Logger logger;

	@Inject
	private ResourceBundle bundle;

	@GET
	@Path("demoiselle/version")
	@Produces("text/plain")
	public String getDemoiselleVersion() {
		return Metadata.getVersion();
	}

	@GET
	@Path("version")
	@Produces("text/plain")
	public String getAppVersion() throws Exception {
		String key = "application.version";

		if (!bundle.containsKey(key)) {
			// logger.debug();

			throw new ServiceUnavailableException();
		}

		return bundle.getString(key);
	}

	@GET
	@Path("message/{key}")
	@Produces(TEXT_HTML)
	public String getMessage(@PathParam("key") String key) throws Exception {
		if (!bundle.containsKey(key)) {
			throw new NotFoundException();
		}

		return bundle.getString(key);
	}
}
