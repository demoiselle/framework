package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;

import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.internal.management.ManagementNotificationEventImpl;
import br.gov.frameworkdemoiselle.internal.management.qualifier.AttributeChange;
import br.gov.frameworkdemoiselle.internal.management.qualifier.Generic;
import br.gov.frameworkdemoiselle.management.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.ManagementNotificationEvent;
import br.gov.frameworkdemoiselle.management.GenericNotification;
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
