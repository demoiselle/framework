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
package management.notification;

import javax.inject.Inject;

import junit.framework.Assert;
import management.testclasses.DummyManagedClass;
import management.testclasses.DummyNotificationListener;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.implementation.ManagedType;
import br.gov.frameworkdemoiselle.internal.implementation.Management;
import br.gov.frameworkdemoiselle.management.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.GenericNotification;
import br.gov.frameworkdemoiselle.management.NotificationManager;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Test the {@link NotificationManager} with a dummy extension to check if notifications are correctly propagated
 * 
 * @author SERPRO
 */
@RunWith(Arquillian.class)
public class NotificationTest {

	@Inject
	private NotificationManager manager;

	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;

	@Deployment
	public static JavaArchive createDeployment() {
		return Tests.createDeployment(NotificationTest.class)
				.addClasses(DummyNotificationListener.class, DummyManagedClass.class);
	}

	/**
	 * Test sending a normal notification
	 */
	@Test
	public void sendGenericNotification() {
		manager.sendNotification(new GenericNotification("Test Message"));
		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals("Test Message", listener.getMessage());
	}

	/**
	 * Test sending a notification of change in attribute
	 */
	@Test
	public void sendAttributeChangeNotification() {
		manager.sendNotification(new AttributeChangeNotification("Test Message", "attribute", String.class, "old",
				"new"));
		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals("Test Message - attribute", listener.getMessage());
	}

	/**
	 * Test if notifications are automatically sent when an attribute from a managed class change values
	 */
	@Test
	public void notifyChangeManagedClass() {
		Management manager = Beans.getReference(Management.class);

		for (ManagedType type : manager.getManagedTypes()) {
			if (type.getType().equals(DummyManagedClass.class)) {
				manager.setProperty(type, "id", new Integer(10));
				break;
			}
		}

		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals(
				bundle.getString("management-notification-attribute-changed", "id",
						DummyManagedClass.class.getCanonicalName())
						+ " - id", listener.getMessage());
	}

}
