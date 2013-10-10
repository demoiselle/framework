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
package management.tests.notification;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
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

import br.gov.frameworkdemoiselle.internal.configuration.JMXConfig;
import br.gov.frameworkdemoiselle.internal.implementation.MBeanManager;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.lifecycle.AfterStartupProccess;
import br.gov.frameworkdemoiselle.management.AttributeChangeMessage;
import br.gov.frameworkdemoiselle.management.DefaultNotification;
import br.gov.frameworkdemoiselle.management.Notification;
import br.gov.frameworkdemoiselle.management.NotificationManager;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class NotificationBroadcasterTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return Tests.createDeployment(NotificationBroadcasterTest.class)
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
	 * Testa o envio de uma mensagem para clientes conectados
	 */
	@Test
	public void sendMessage() {
		JMXConfig config = Beans.getReference(JMXConfig.class);

		// Este será o lado cliente. Este manager é usado para enviar notificações a partir do código da aplicação
		NotificationManager notificationManager = Beans.getReference(NotificationManager.class);

		// Obtém o servidor MBean onde anexaremos um listener para a notificação
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Aqui obtemos o MBean de notificações já registrado pelo bootstrap
		StringBuffer notificationMBeanName = new StringBuffer()
				.append(config.getNotificationDomain() != null ? config.getNotificationDomain()
						: "br.gov.frameworkdemoiselle.jmx").append(":name=").append(config.getNotificationMBeanName());

		ObjectName name = null;
		try {
			name = new ObjectName(notificationMBeanName.toString());
		} catch (MalformedObjectNameException e) {
			Assert.fail();
		}

		// StringBuffer vazio
		StringBuffer notificationBuffer = new StringBuffer();

		// Este notification listener será chamado quando o Demoiselle enviar a notificação e vai colocar
		// a mensagem enviada em "notificationBuffer"
		NotificationListener listener = new TestNotificationListener(notificationBuffer);

		try {
			// Anexa o listener no servidor MBean
			server.addNotificationListener(name, listener, null, null);
		} catch (InstanceNotFoundException e) {
			Assert.fail();
		}

		// Manda a notificação pelo Demoiselle
		DefaultNotification n = new DefaultNotification("Notification test successful");
		notificationManager.sendNotification(n);

		// Se o componente funcionou, o Demoiselle propagou a notificação para o servidor MBean e o listener preencheu
		// o StringBuffer com nossa mensagem.
		Assert.assertEquals("Notification test successful", notificationBuffer.toString());
		
	}

	/**
	 * Testa o envio de uma mensagem de mudança de atributo para clientes conectados
	 */
	@Test
	public void sendAttributeChangedMessage() {
		
		JMXConfig config = Beans.getReference(JMXConfig.class);

		// Obtém o servidor MBean onde anexaremos um listener para a notificação
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		NotificationManager notificationManager = Beans.getReference(NotificationManager.class);
		MBeanManager mBeanManager = Beans.getReference(MBeanManager.class);

		// Aqui obtemos o MBean de notificações já registrado pelo bootstrap
		StringBuffer notificationMBeanName = new StringBuffer()
				.append(config.getNotificationDomain() != null ? config.getNotificationDomain()
						: "br.gov.frameworkdemoiselle.jmx").append(":name=").append(config.getNotificationMBeanName());
		ObjectInstance instance = mBeanManager.findMBeanInstance(notificationMBeanName.toString());

		// StringBuffer vazio
		StringBuffer notificationBuffer = new StringBuffer();

		// Este notification listener será chamado quando o Demoiselle enviar a notificação e vai colocar
		// a mensagem enviada em "notificationBuffer"
		NotificationListener listener = new TestNotificationListener(notificationBuffer);

		try {
			// Anexa o listener no servidor MBean
			server.addNotificationListener(instance.getObjectName(), listener, null, null);
		} catch (InstanceNotFoundException e) {
			Assert.fail();
		}

		// Manda a notificação pelo Demoiselle
		Notification notification = new DefaultNotification( new AttributeChangeMessage("Attribute Changed", "name", String.class, "Demoiselle 1", "Demoiselle 2") );
		notificationManager.sendNotification(notification);

		// Se o componente funcionou, o Demoiselle propagou a notificação para o servidor MBean e o listener preencheu
		// o StringBuffer com nossa mensagem.
		Assert.assertEquals("Attribute Changed: name = Demoiselle 2", notificationBuffer.toString());
		
	}

	/**
	 * Implementação do {@link NotificationListener} do Java que vai por qualquer notificação recebida em um
	 * StringBuffer.
	 * 
	 * @author serpro
	 */
	class TestNotificationListener implements NotificationListener {

		StringBuffer message;

		public TestNotificationListener(StringBuffer testMessage) {
			this.message = testMessage;
		}

		@Override
		public void handleNotification(javax.management.Notification notification, Object handback) {
			if (message != null) {
				message.append(notification.getMessage());

				if (notification instanceof javax.management.AttributeChangeNotification) {
					message.append(": ")
							.append(((javax.management.AttributeChangeNotification) notification).getAttributeName())
							.append(" = ")
							.append(((javax.management.AttributeChangeNotification) notification).getNewValue());
				}
			}
		}

	}

}
