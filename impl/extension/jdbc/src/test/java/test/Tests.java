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
package test;

import java.io.File;
import java.util.Locale;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;

import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;
import br.gov.frameworkdemoiselle.internal.producer.ConnectionProducer;
import br.gov.frameworkdemoiselle.internal.producer.DataSourceProducer;
import br.gov.frameworkdemoiselle.internal.proxy.BasicDataSourceProxy;
import br.gov.frameworkdemoiselle.internal.proxy.ConnectionProxy;
import br.gov.frameworkdemoiselle.transaction.JDBCTransaction;

@Ignore
public final class Tests {

	private Tests() {
	}

	public static WebArchive createDeployment(final Class<?> baseClass) {
		return createDeployment().addPackages(true, baseClass.getPackage());
	}

	private static WebArchive createDeployment() {
		File[] libs = Maven.resolver().offline().loadPomFromFile("pom.xml", "arquillian-test")
				.importCompileAndRuntimeDependencies().importTestDependencies().resolve().withTransitivity().asFile();

		return ShrinkWrap
				.create(WebArchive.class)
				.addClass(Tests.class)
				.addClass(JDBCConfig.class)
				.addClass(ConnectionProducer.class)
				.addClass(DataSourceProducer.class)
				.addClass(BasicDataSourceProxy.class)
				.addClass(ConnectionProxy.class)
				.addClass(JDBCTransaction.class)
				.addAsResource(createFileAsset("src/main/resources/demoiselle-jdbc-bundle.properties"),
						"demoiselle-jdbc-bundle.properties")
				.addAsWebInfResource(createFileAsset("src/test/resources/test/beans.xml"), "beans.xml")
				.addAsLibraries(libs);
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
