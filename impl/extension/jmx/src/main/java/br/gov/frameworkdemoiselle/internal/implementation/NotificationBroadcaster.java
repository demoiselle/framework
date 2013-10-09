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
package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import br.gov.frameworkdemoiselle.internal.configuration.JMXConfig;
import br.gov.frameworkdemoiselle.management.AttributeChangeMessage;
import br.gov.frameworkdemoiselle.management.ManagementNotificationEvent;
import br.gov.frameworkdemoiselle.management.NotificationManager;

/**
 * Implementation of the {@link NotificationBroadcaster} MBean.
 * When the {@link NotificationManager} sends an event, a {@link NotificationEventListener} captures the notification and uses
 * this MBean to send it as a JMX notification.
 * 
 * @author serpro
 *
 */
public final class NotificationBroadcaster extends NotificationBroadcasterSupport implements NotificationBroadcasterMBean,Serializable {
	
	private static final long serialVersionUID = 1L;

	private AtomicInteger sequence = new AtomicInteger();

	private static final String NOTIFICATION_TYPE_GENERIC = "jmx.message";

	protected void sendNotification( ManagementNotificationEvent event , JMXConfig config ) {
		br.gov.frameworkdemoiselle.management.Notification demoiselleNotification = event.getNotification();
		Object message = demoiselleNotification.getMessage();
		Notification n;
		
		if (AttributeChangeMessage.class.isInstance( message )){
			AttributeChangeMessage attributeChangeMessage = (AttributeChangeMessage) message;
			
			n = new AttributeChangeNotification(config.getNotificationMBeanName(), sequence.incrementAndGet()
					, System.currentTimeMillis(), attributeChangeMessage.getDescription()
					, attributeChangeMessage.getAttributeName(), attributeChangeMessage.getAttributeType().getSimpleName()
					, attributeChangeMessage.getOldValue(), attributeChangeMessage.getNewValue());
		}
		else{
			n = new Notification(NOTIFICATION_TYPE_GENERIC, config.getNotificationMBeanName(), sequence.incrementAndGet(), System.currentTimeMillis(), demoiselleNotification.getMessage().toString());
		}
		sendNotification(n);
	}
}
