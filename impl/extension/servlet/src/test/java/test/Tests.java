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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;

import br.gov.frameworkdemoiselle.internal.producer.HttpServletRequestProducer;
import br.gov.frameworkdemoiselle.internal.producer.HttpServletResponseProducer;
import br.gov.frameworkdemoiselle.internal.producer.HttpSessionProducer;
import br.gov.frameworkdemoiselle.internal.producer.ServletLocaleProducer;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.ServletAuthenticator;
import br.gov.frameworkdemoiselle.security.ServletAuthorizer;
//import br.gov.frameworkdemoiselle.util.BasicAuthFilter;
import br.gov.frameworkdemoiselle.util.ServletFilter;
import br.gov.frameworkdemoiselle.util.ServletListener;
import br.gov.frameworkdemoiselle.util.SessionListener;

@Ignore
public final class Tests {

	private Tests() {
	}

	public static WebArchive createDeployment(final Class<?> baseClass) {
		return createDeployment().addPackages(true, baseClass.getPackage()).addClass(Tests.class);
	}

	public static WebArchive createDeployment() {
		File[] libs = Maven.resolver().offline().loadPomFromFile("pom.xml", "arquillian-test")
				.importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile();

		return ShrinkWrap
				.create(WebArchive.class)
				.addClass(Credentials.class)
				.addClass(ServletAuthenticator.class)
				.addClass(ServletAuthorizer.class)
				.addClass(ServletFilter.class)
				.addClass(ServletListener.class)
				.addClass(SessionListener.class)
				.addClass(HttpServletRequestProducer.class)
				.addClass(HttpServletResponseProducer.class)
				.addClass(HttpSessionProducer.class)
				.addClass(ServletLocaleProducer.class)
//				.addClass(BasicAuthFilter.class)
				.addAsResource(createFileAsset("src/main/resources/demoiselle-servlet-bundle.properties"),
						"demoiselle-servlet-bundle.properties")
				.addAsWebInfResource(createFileAsset("src/test/resources/test/beans.xml"), "beans.xml")
				.addAsLibraries(libs);
	}

	public static FileAsset createFileAsset(final String pathname) {
		return new FileAsset(new File(pathname));
	}
}
