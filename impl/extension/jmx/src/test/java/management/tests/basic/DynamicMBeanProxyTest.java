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
package management.tests.basic;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.Assert;
import management.domain.ManagedTestClass;
import management.tests.Tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.internal.implementation.MBeanManager;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.lifecycle.AfterStartupProccess;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class DynamicMBeanProxyTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return Tests.createDeployment(DynamicMBeanProxyTest.class)
				.addClasses(ManagedTestClass.class);
	}
	
	@BeforeClass
	public static void fireEventStartupProccess() {
		Beans.getBeanManager().fireEvent(new AfterStartupProccess() {
		});
	}
	
	@AfterClass
	public static void fireEventShutdownProccess() {
		Beans.getBeanManager().fireEvent(new AfterShutdownProccess() {
		});
	}	
	
	/**
	 * Testa se o bootstrap está corretamente carregando e registrando classes anotadas com {@link Managed} como MBeans.
	 */
	@Test
	public void initializeMBean() {
		MBeanManager manager = Beans.getReference(MBeanManager.class);

		Assert.assertNotNull(manager);
		Assert.assertNotNull(manager.listRegisteredMBeans());

		// O componente demoiselle-jmx sempre tem pelo menos um MBean, que é
		// o NotificationBroadcaster. Qualquer classe gerenciada criada pelo usuário
		// será adicionada a ela, então esperamos 2 MBeans aqui.
		Assert.assertEquals(2, manager.listRegisteredMBeans().size());
		
	}

	@Test
	public void attributeWrite() {
		
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		ObjectName name = null;
		try {
			name = new ObjectName("br.gov.frameworkdemoiselle.jmx.domain:name=ManagedTest");
		} catch (MalformedObjectNameException e) {
			Assert.fail();
		}

		try {
			server.setAttribute(name, new Attribute("attribute", "New Value"));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
	}

	@Test
	public void attributeRead() {
		
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		ObjectName name = null;
		try {
			name = new ObjectName("br.gov.frameworkdemoiselle.jmx.domain:name=ManagedTest");
		} catch (MalformedObjectNameException e) {
			Assert.fail();
		}

		try {
			server.setAttribute(name, new Attribute("attribute", "New Value"));

			Object info = server.getAttribute(name, "attribute");

			Assert.assertEquals("New Value", info);
		} catch (Exception e) {
			Assert.fail();
		}
		
	}

	@Test
	public void operationInvocation() {

		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		ObjectName name = null;
		try {
			name = new ObjectName("br.gov.frameworkdemoiselle.jmx.domain:name=ManagedTest");
		} catch (MalformedObjectNameException e) {
			Assert.fail();
		}

		try {
			server.setAttribute(name, new Attribute("attribute", "Defined Value"));

			Object info = server.invoke(name, "operation", new Object[] { "Test" }, new String[] { "String" });
			Assert.assertEquals("Operation called with parameter=Test. Current attribute value is Defined Value", info);
		} catch (Exception e) {
			Assert.fail();
		}

	}

	@Test
	public void tryWriteOverReadOnly() {
		
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		ObjectName name = null;
		try {
			name = new ObjectName("br.gov.frameworkdemoiselle.jmx.domain:name=ManagedTest");
		} catch (MalformedObjectNameException e) {
			Assert.fail();
		}

		try {
			server.setAttribute(name, new Attribute("attribute", "New Value"));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
	}
}
