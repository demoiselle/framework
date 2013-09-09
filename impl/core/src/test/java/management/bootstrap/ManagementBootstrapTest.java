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
package management.bootstrap;

import java.util.List;

import management.testclasses.DummyManagedClass;
import management.testclasses.DummyManagementExtension;
import management.testclasses.ManagedClassStore;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.implementation.ManagedType;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.management.ManagementExtension;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class ManagementBootstrapTest {

	/**
	 * Deployment to test normal deployment behaviour
	 * 
	 */
	@Deployment
	public static JavaArchive createDeployment() {
		return Tests.createDeployment(ManagementBootstrapTest.class)
				.addClasses(DummyManagementExtension.class,
						DummyManagedClass.class, ManagedClassStore.class);
	}

	/**
	 * Test if a a management extension (a library that implements
	 * {@link ManagementExtension}) is correctly detected.
	 */
	@Test
	public void managementExtensionRegistration() {
		// "store" é application scoped e é usado pelo DummyManagementExtension
		// para
		// armazenar todos os beans anotados com @ManagementController. Se o
		// bootstrap rodou corretamente,
		// ele chamou DummyManagementExtension.initialize e este store conterá o
		// bean de teste que anotamos.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		Assert.assertEquals(1, store.getManagedTypes().size());
	}

	/**
	 * Test if a a management extension's shutdown method is correctly called
	 * upon application shutdown.
	 */
	@Test
	public void managementExtensionShutdown() {
		// "store" é application scoped e é usado pelo DummyManagementExtension
		// para
		// armazenar todos os beans anotados com @ManagementController. Se o
		// bootstrap rodou corretamente,
		// ele chamou DummyManagementExtension.initialize e este store conterá o
		// bean de teste que anotamos.
		// Nós então disparamos o evento de shutdown onde ele deverá limpar o
		// store.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		// Detecta se a classe anotada foi detectada
		List<ManagedType> managedTypes = store.getManagedTypes();
		Assert.assertEquals(1, managedTypes.size());

		Beans.getBeanManager().fireEvent(new AfterShutdownProccess() {
		});

		// Após o "undeploy", o ciclo de vida precisa ter removido a classe
		// gerenciada da lista.
		Assert.assertEquals(0, managedTypes.size());
	}

}
