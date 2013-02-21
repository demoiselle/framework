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
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * Configuration class responsible for retrieving specific entity manager parameter values from properties file.
 */
@Configuration(prefix = "frameworkdemoiselle.persistence.")
public class EntityManagerConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	// TODO Implementação apenas para manter a compatibilidade entre a versão 2.3 com a 2.4.
	@Name("unit.name")
	private String persistenceUnitName;

	@Name("default.unit.name")
	private String defaultPersistenceUnitName;

	/**
	 * Getter for persistence unit name.
	 */
	// TODO Implementação apenas para manter a compatibilidade entre a versão 2.3 com a 2.4.
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	/**
	 * Getter for persistence unit name.
	 */
	public String getDefaultPersistenceUnitName() {
		// TODO Implementação apenas para manter a compatibilidade entre a versão 2.3 com a 2.4.
		String persistenceUnitName = getPersistenceUnitName();
		if (!Strings.isEmpty(persistenceUnitName)) {
			Logger logger = Beans.getReference(Logger.class);
			logger.info("A propriedade frameworkdemoiselle.persistence.unit.name="
					+ persistenceUnitName
					+ " não será suportada nas próximas versões do framework. Para evitar futuros problemas atualize a propriedade para frameworkdemoiselle.persistence.default.unit.name="
					+ persistenceUnitName);

			return persistenceUnitName;
		}

		return defaultPersistenceUnitName;
	}
}
