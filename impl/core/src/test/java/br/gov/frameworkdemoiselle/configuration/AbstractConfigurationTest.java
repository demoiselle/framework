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
package br.gov.frameworkdemoiselle.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.bootstrap.ConfigurationBootstrap;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationArrayValueExtractor;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationClassValueExtractor;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationMapValueExtractor;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationPrimitiveOrWrapperValueExtractor;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationStringValueExtractor;
import br.gov.frameworkdemoiselle.internal.producer.LocaleProducer;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;

public abstract class AbstractConfigurationTest {

	protected static Class<?>[] getConfigurationClasses() {
		List<Class<?>> result = new ArrayList<Class<?>>();

		result.add(Ignore.class);
		result.add(Name.class);
		result.add(Configuration.class);
		result.add(CoreBootstrap.class);
		result.add(ConfigurationBootstrap.class);
		result.add(ConfigurationLoader.class);
		result.add(ConfigurationValueExtractor.class);
		result.add(ConfigurationArrayValueExtractor.class);
		result.add(ConfigurationMapValueExtractor.class);
		result.add(ConfigurationClassValueExtractor.class);
		result.add(ConfigurationStringValueExtractor.class);
		result.add(ConfigurationPrimitiveOrWrapperValueExtractor.class);
		result.add(Beans.class);
		result.add(ResourceBundleProducer.class);
		result.add(LoggerProducer.class);
		result.add(LocaleProducer.class);

		return result.toArray(new Class<?>[0]);
	}

	public static JavaArchive createConfigurationDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClasses(getConfigurationClasses())
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource(
						new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension");
	}
}
