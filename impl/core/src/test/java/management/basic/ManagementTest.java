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
package management.basic;

import junit.framework.Assert;
import management.testclasses.DummyManagedClass;
import management.testclasses.DummyManagementExtension;
import management.testclasses.ManagedClassStore;
import management.testclasses.RequestScopeBeanClient;
import management.testclasses.RequestScopedClass;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Test case that simulates a management extension and tests if properties and operations on a managed class can be
 * easily accessed and invoked.
 * 
 * @author SERPRO
 */
@RunWith(Arquillian.class)
public class ManagementTest {

	@Deployment
	public static JavaArchive createMultithreadedDeployment() {
		
		return Tests.createDeployment(ManagementTest.class)
				.addClasses(DummyManagementExtension.class, DummyManagedClass.class, ManagedClassStore.class,RequestScopeBeanClient.class, RequestScopedClass.class);
	}

	@Test
	public void readProperty() {
		DummyManagedClass managedClass = Beans.getReference(DummyManagedClass.class);
		managedClass.setName("Test AmbiguousQualifier");

		// store é nossa extensão de gerenciamento falsa, então estamos testando um "cliente" acessando
		// nosso tipo gerenciado DummyManagedClass remotamente.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);
		Object name = store.getProperty(DummyManagedClass.class, "name");
		Assert.assertEquals("Test AmbiguousQualifier", name);
	}

	@Test
	public void writeProperty() {
		// store é nossa extensão de gerenciamento falsa, então estamos testando um "cliente" definindo
		// um novo valor em uma propriedade de nosso tipo gerenciado DummyManagedClass remotamente.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);
		store.setProperty(DummyManagedClass.class, "name", "Test AmbiguousQualifier");

		DummyManagedClass managedClass = Beans.getReference(DummyManagedClass.class);
		Assert.assertEquals("Test AmbiguousQualifier", managedClass.getName());
	}

	@Test
	public void readAWriteOnly() {

		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		try {
			store.getProperty(DummyManagedClass.class, "writeOnlyProperty");
			Assert.fail();
		} catch (DemoiselleException de) {
			// SUCCESS
		}

	}

	@Test
	public void writeAReadOnly() {

		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		try {
			store.setProperty(DummyManagedClass.class, "readOnlyProperty", "New Value");
			Assert.fail();
		} catch (DemoiselleException de) {
			// SUCCESS
		}

	}

	@Test
	public void invokeOperation() {

		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		try {
			store.setProperty(DummyManagedClass.class, "firstFactor", new Integer(10));
			store.setProperty(DummyManagedClass.class, "secondFactor", new Integer(15));
			Integer response = (Integer) store.invoke(DummyManagedClass.class, "sumFactors");
			Assert.assertEquals(new Integer(25), response);
		} catch (DemoiselleException de) {
			Assert.fail(de.getMessage());
		}

	}

	@Test
	public void invokeNonAnnotatedOperation() {

		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		try {
			// O método "nonOperationAnnotatedMethod" existe na classe DummyManagedClass, mas não está anotado como
			// "@ManagedOperation", então
			// ela não pode ser exposta para extensões.
			store.invoke(DummyManagedClass.class, "nonOperationAnnotatedMethod");
			Assert.fail();
		} catch (DemoiselleException de) {
			// SUCCESS
		}

	}

	@Test
	public void accessLevelControl() {
		// tentamos escrever em uma propriedade que, apesar de ter método setter, está marcada como read-only.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		try {
			store.setProperty(DummyManagedClass.class, "readOnlyPropertyWithSetMethod", "A Value");
			Assert.fail();
		} catch (DemoiselleException de) {
			System.out.println(de.getMessage());
			// success
		}
	}

	@Test
	public void requestScopedOperation() {
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

		// Esta operação faz multiplos acessos a um bean RequestScoped. Durante a operação todos os acessos devem
		// operar sob a mesma instância, mas uma segunda invocação deve operar em uma instância nova
		Object info = store.invoke(DummyManagedClass.class, "requestScopedOperation");
		Assert.assertEquals("-OPERATION ONE CALLED--OPERATION TWO CALLED-", info);

		// Segunda invocação para testar se uma nova instância é criada, já que esse é um novo request.
		info = store.invoke(DummyManagedClass.class, "requestScopedOperation");
		Assert.assertEquals("-OPERATION ONE CALLED--OPERATION TWO CALLED-", info);
	}
}
