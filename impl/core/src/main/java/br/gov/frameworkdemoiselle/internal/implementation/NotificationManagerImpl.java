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

import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.management.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.GenericNotification;
import br.gov.frameworkdemoiselle.management.ManagementNotificationEvent;
import br.gov.frameworkdemoiselle.management.NotificationManager;
import br.gov.frameworkdemoiselle.util.Beans;


@SuppressWarnings("serial")
public class NotificationManagerImpl implements NotificationManager,Serializable {
	
	@Inject
	@Generic
	private Event<ManagementNotificationEvent> genericNotificationEvent;
	
	@Inject
	@AttributeChange
	private Event<ManagementNotificationEvent> attributeChangeNotificationEvent;
	
	/**
	 * Sends a generic notification to all management clients.
	 * 
	 * @param notification The notification to send
	 */
	public void sendNotification(GenericNotification notification) {
		if (! AttributeChangeNotification.class.isInstance(notification) ){
			getGenericNotificationEvent().fire(new ManagementNotificationEventImpl(notification));
		}
		else{
			getAttributeChangeNotificationEvent().fire(new ManagementNotificationEventImpl(notification));
		}
	}

	@SuppressWarnings("unchecked")
	private Event<ManagementNotificationEvent> getGenericNotificationEvent() {
		if (genericNotificationEvent==null){
			genericNotificationEvent = Beans.getReference(Event.class , new AnnotationLiteral<Generic>() {});
		}
		
		return genericNotificationEvent;
	}
	
	@SuppressWarnings("unchecked")
	private Event<ManagementNotificationEvent> getAttributeChangeNotificationEvent() {
		if (attributeChangeNotificationEvent==null){
			attributeChangeNotificationEvent = Beans.getReference(Event.class , new AnnotationLiteral<AttributeChange>() {});
		}
		
		return attributeChangeNotificationEvent;
	}
	
	
	
}
