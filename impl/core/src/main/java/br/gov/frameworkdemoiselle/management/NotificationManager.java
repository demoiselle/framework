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
package br.gov.frameworkdemoiselle.management;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.internal.management.ManagementNotificationEvent;
import br.gov.frameworkdemoiselle.internal.management.qualifier.AttributeChange;
import br.gov.frameworkdemoiselle.internal.management.qualifier.Generic;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * 
 * <p>Central class to manage sending notifications to management clients.
 * This class allows applications to send management notifications without
 * knowledge of the technology used to send those notifications.</p>
 * 
 * <p>To obtain an instance of the {@link NotificationManager} simply inject it in
 * your code using {@link Inject} or the {@link Beans#getReference(Class beanType)} method. The {@link NotificationManager}
 * is {@link ApplicationScoped}, so you can inject it as many times as needed and still have only one instance per application.</p>
 * 
 * <p>Implementators of management protocols must observe the {@link ManagementNotificationEvent} event (using the {@link Observes} annotation), this way
 * they will receive an event containing the original notification and can translate this notification to a specific protocol. Optionaly,
 * the implementator can use qualifiers like the {@link Generic} and {@link AttributeChange} qualifiers
 * to filter what king of notifications they will handle. One example of an implementator is the <b>demoiselle-jmx</b> extension.</p>
 * 
 * @author serpro
 *
 */
@ApplicationScoped
@SuppressWarnings("serial")
public class NotificationManager implements Serializable{
	
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
	public void sendNotification(Notification notification) {
		if (! AttributeChangeNotification.class.isInstance(notification) ){
			getGenericNotificationEvent().fire(new ManagementNotificationEvent(notification));
		}
		else{
			getAttributeChangeNotificationEvent().fire(new ManagementNotificationEvent(notification));
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
