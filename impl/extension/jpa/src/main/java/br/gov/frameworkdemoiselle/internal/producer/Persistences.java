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
package br.gov.frameworkdemoiselle.internal.producer;

import static br.gov.frameworkdemoiselle.configuration.Configuration.DEFAULT_RESOURCE;

import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Singleton
public class Persistences {

	@Inject
	protected Logger logger;

	@Inject
	@Name("demoiselle-jpa-bundle")
	protected ResourceBundle bundle;

	@Inject
	private EntityManagerFactoryProducer factory;

	/**
	 * Tries to get persistence unit name from demoiselle.properties.
	 * 
	 * @param config
	 *            Configuration containing persistence unit name.
	 * @return Persistence unit name.
	 */
	protected String getFromProperties(EntityManagerConfig config) {
		String persistenceUnit = config.getDefaultPersistenceUnitName();

		if (persistenceUnit != null) {
			this.logger.fine(bundle.getString("getting-persistence-unit-from-properties", DEFAULT_RESOURCE));
		}

		return persistenceUnit;
	}

	/**
	 * Uses persistence.xml to get informations about which persistence unit to use. Throws DemoiselleException if more
	 * than one Persistence Unit is defined.
	 * 
	 * @return Persistence Unit AmbiguousQualifier
	 */
	protected String getFromXML() {
		Set<String> persistenceUnits = factory.getCache().keySet();

		if (persistenceUnits.size() > 1) {
			throw new DemoiselleException(bundle.getString("more-than-one-persistence-unit-defined",
					Name.class.getSimpleName()));
		} else {
			return persistenceUnits.iterator().next();
		}
	}
}
