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

package management.tests;

import java.io.File;
import java.util.Locale;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;

@Ignore
public final class Tests {

	private Tests() {
	}

	public static JavaArchive createDeployment(final Class<?> baseClass) {
		return createDeployment().addPackages(true, baseClass.getPackage());
	}

	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClass(Tests.class)
				.addPackages(true, "br")
				.addAsResource(Tests.createFileAsset("src/main/resources/demoiselle-jmx-bundle.properties") , "demoiselle-jmx-bundle.properties")
				.addAsResource(Tests.createFileAsset("src/test/resources/log4j.properties"),"log4j.properties")
				.addAsResource(Tests.createFileAsset("src/test/resources/configuration/demoiselle.properties"),"demoiselle.properties")
				.addAsManifestResource(Tests.createFileAsset("src/test/resources/beans.xml"), "beans.xml")
				.addAsManifestResource(
						new File("src/test/resources/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension");
	}

	public static FileAsset createFileAsset(final String pathname) {
		return new FileAsset(new File(pathname));
	}

	@Default
	@Produces
	public Locale create() {
		return Locale.getDefault();
	}
}
