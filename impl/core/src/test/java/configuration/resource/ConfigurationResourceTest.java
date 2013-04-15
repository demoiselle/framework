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
package configuration.resource;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import configuration.AbstractConfigurationTest;

@RunWith(Arquillian.class)
public class ConfigurationResourceTest extends AbstractConfigurationTest {

	@Inject
	private PropertiesDefaultFileConfig propDefault;

	@Inject
	private PropertiesNamedDefaultFileConfig propNamedDefault;

	@Inject
	private PropertiesNotDefaultFileConfig propNotDefault;

	@Inject
	private PropertiesWithoutFileConfig propWithoutFile;

	@Inject
	private XMLDefaultFileConfig xmlDefault;

	@Inject
	private XMLNamedDefaultFileConfig xmlNamedDefault;

	@Inject
	private XMLNotDefaultFileConfig xmlNotDefault;

	@Inject
	private XMLWithoutFileConfig xmlWithoutFile;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = createConfigurationDeployment();

		deployment.addPackages(true, ConfigurationResourceTest.class.getPackage());
		deployment
				.addAsResource(
						new FileAsset(new File("src/test/resources/configuration/resource/demoiselle.properties")),
						"demoiselle.properties")
				.addAsResource(new FileAsset(new File("src/test/resources/configuration/resource/demoiselle.xml")),
						"demoiselle.xml")
				.addAsResource(
						new FileAsset(new File("src/test/resources/configuration/resource/resource.properties")),
						"resource.properties")
				.addAsResource(new FileAsset(new File("src/test/resources/configuration/resource/resource.xml")),
						"resource.xml");

		return deployment;
	}

	@Test
	public void loadFromDefaultFile() {
		String expected = "demoiselle";

		assertEquals(expected, propDefault.getStringWithComma());
		assertEquals(expected, xmlDefault.getStringWithComma());
	}

	@Test
	public void loadFromNamedDefaultFile() {
		String expected = "demoiselle";

		assertEquals(expected, propNamedDefault.getStringWithComma());
		assertEquals(expected, xmlNamedDefault.getStringWithComma());
	}

	@Test
	public void loadFromNotDefaultFile() {
		String expected = "demoiselle";

		assertEquals(expected, propNotDefault.getStringWithComma());
		assertEquals(expected, xmlNotDefault.getStringWithComma());
	}

	@Test
	public void loadFromNonexistentFile() {
		String expected = null;

		assertEquals(expected, propWithoutFile.getStringWithComma());
		assertEquals(expected, xmlWithoutFile.getStringWithComma());
	}
}
