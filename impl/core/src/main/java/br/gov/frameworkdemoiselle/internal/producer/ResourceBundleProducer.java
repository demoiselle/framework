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

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * This factory creates ResourceBundles with the application scopes.
 * 
 * @author SERPRO
 */
public class ResourceBundleProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * This method should be used by classes that can not inject ResourceBundle, to create the ResourceBundle.
	 * 
	 * @param String
	 *            baseName
	 */
	public static ResourceBundle create(String baseName) {
		return create(baseName, Beans.getReference(Locale.class));
	}

	/**
	 * This method should be used by classes that can not inject ResourceBundle, to create the ResourceBundle.
	 * 
	 * @param String
	 *            baseName
	 */
	public static ResourceBundle create(String baseName, Locale locale) {
		ResourceBundle bundle = null;
		bundle = new ResourceBundle(baseName, locale);
		return bundle;
	}

	/**
	 * This method is the factory default for ResourceBundle. It creates the ResourceBundle based on a file called
	 * messages.properties.
	 */
	@Produces
	@Default
	public ResourceBundle create(InjectionPoint ip) {
		String baseName;

		if (ip != null && ip.getAnnotated().isAnnotationPresent(Name.class)) {
			baseName = ip.getAnnotated().getAnnotation(Name.class).value();
		} else {
			baseName = "messages";
		}

		return create(baseName, Beans.getReference(Locale.class));
	}
}
