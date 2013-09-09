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

import br.gov.frameworkdemoiselle.annotation.NextView;
import br.gov.frameworkdemoiselle.annotation.PreviousView;
import br.gov.frameworkdemoiselle.annotation.Redirect;
import br.gov.frameworkdemoiselle.internal.bootstrap.JsfBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.ExceptionHandlerConfig;
import br.gov.frameworkdemoiselle.internal.configuration.JsfSecurityConfig;
import br.gov.frameworkdemoiselle.internal.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.implementation.AbstractExceptionHandler;
import br.gov.frameworkdemoiselle.internal.implementation.ApplicationExceptionHandler;
import br.gov.frameworkdemoiselle.internal.implementation.ApplicationExceptionHandlerFactory;
import br.gov.frameworkdemoiselle.internal.implementation.AuthenticationExceptionHandler;
import br.gov.frameworkdemoiselle.internal.implementation.AuthenticationExceptionHandlerFactory;
import br.gov.frameworkdemoiselle.internal.implementation.AuthorizationExceptionHandler;
import br.gov.frameworkdemoiselle.internal.implementation.AuthorizationExceptionHandlerFactory;
import br.gov.frameworkdemoiselle.internal.implementation.FacesMessageAppender;
import br.gov.frameworkdemoiselle.internal.implementation.FileRendererImpl;
import br.gov.frameworkdemoiselle.internal.implementation.ParameterImpl;
import br.gov.frameworkdemoiselle.internal.implementation.RedirectExceptionHandler;
import br.gov.frameworkdemoiselle.internal.implementation.RedirectExceptionHandlerFactory;
import br.gov.frameworkdemoiselle.internal.implementation.SecurityObserver;
import br.gov.frameworkdemoiselle.internal.procuder.ParameterProducer;
import br.gov.frameworkdemoiselle.internal.proxy.FacesContextProxy;
import br.gov.frameworkdemoiselle.template.AbstractEditPageBean;
import br.gov.frameworkdemoiselle.template.AbstractListPageBean;
import br.gov.frameworkdemoiselle.template.AbstractPageBean;
import br.gov.frameworkdemoiselle.template.EditPageBean;
import br.gov.frameworkdemoiselle.template.ListPageBean;
import br.gov.frameworkdemoiselle.template.PageBean;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.FileRenderer;
import br.gov.frameworkdemoiselle.util.Locales;
import br.gov.frameworkdemoiselle.util.PageNotFoundException;
import br.gov.frameworkdemoiselle.util.Parameter;
import br.gov.frameworkdemoiselle.util.Redirector;

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
				.addClass(PageNotFoundException.class)
				.addClass(Locales.class)
				.addClass(Parameter.class)
				.addClass(Faces.class)
				.addClass(Redirector.class)
				.addClass(FileRenderer.class)
				.addClass(JsfSecurityConfig.class)
				.addClass(ExceptionHandlerConfig.class)
				.addClass(ViewContext.class)
				.addClass(AuthorizationExceptionHandlerFactory.class)
				.addClass(ApplicationExceptionHandler.class)
				.addClass(FileRendererImpl.class)
				.addClass(SecurityObserver.class)
				.addClass(FacesMessageAppender.class)
				.addClass(RedirectExceptionHandler.class)
				.addClass(AuthenticationExceptionHandlerFactory.class)
				.addClass(AuthenticationExceptionHandler.class)
				.addClass(ApplicationExceptionHandlerFactory.class)
				.addClass(ParameterImpl.class)
				.addClass(AuthorizationExceptionHandler.class)
				.addClass(RedirectExceptionHandlerFactory.class)
				.addClass(AbstractExceptionHandler.class)
				.addClass(FacesContextProxy.class)
				.addClass(JsfBootstrap.class)
				.addClass(ParameterProducer.class)
				.addClass(AbstractPageBean.class)
				.addClass(ListPageBean.class)
				.addClass(AbstractListPageBean.class)
				.addClass(AbstractEditPageBean.class)
				.addClass(PageBean.class)
				.addClass(EditPageBean.class)
				.addClass(PreviousView.class)
				.addClass(Redirect.class)
				.addClass(NextView.class)
				.addAsResource(createFileAsset("src/main/resources/demoiselle-jsf-bundle.properties"),
						"demoiselle-jsf-bundle.properties")
				.addAsWebInfResource(createFileAsset("src/test/resources/test/beans.xml"), "beans.xml")
				.addAsLibraries(libs);
	}

	public static FileAsset createFileAsset(final String pathname) {
		return new FileAsset(new File(pathname));
	}
}
