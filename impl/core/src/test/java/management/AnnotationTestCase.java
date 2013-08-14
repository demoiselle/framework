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
package management;

import java.io.File;

import management.testclasses.DummyManagementExtension;
import management.testclasses.ManagedClassStore;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.LocaleProducer;

//TODO O arquillian está com um problema onde, embora os testes rodem todos individualmente,
//ao pedir para rodar todos este teste individual causa todos os testes executados após esse
//falharem. Até este problema ser resolvido este teste será ignorado.
@RunWith(Arquillian.class)
@Ignore
public class AnnotationTestCase {

	/**
	 * Deployment containing a malformed managed class. Tests using this deployment will check if deployment fails (it
	 * has to).
	 */
	@Deployment(name = "wrong_annotation", managed = false)
	public static JavaArchive createWrongAnnotationDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClass(LocaleProducer.class)
				.addPackages(true, "br")
				.addAsResource(new FileAsset(new File("src/test/resources/beans.xml")), "beans.xml")
				.addAsManifestResource(
						new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension")
				.addPackages(false, ManagementBootstrapTestCase.class.getPackage())
				//.addClasses(DummyManagementExtension.class, DummyManagedClassPropertyError.class,	ManagedClassStore.class);
				.addClasses(DummyManagementExtension.class, ManagedClassStore.class);
	}

	@Test
	public void testWrongAnnotation(@ArquillianResource Deployer deployer) {

		try {
			deployer.deploy("wrong_annotation");

			// O processo de deploy precisa falhar, pois temos uma classe anotada com falhas.
			Assert.fail();
		} catch (Exception e) {
			//SUCCESS
		} finally {
			deployer.undeploy("wrong_annotation");
		}
	}

}
