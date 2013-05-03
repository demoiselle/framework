package br.gov.frameworkdemoiselle.management.notification;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.management.internal.notification.event.NotificationEvent;
import br.gov.frameworkdemoiselle.management.internal.notification.qualifier.AttributeChange;
import br.gov.frameworkdemoiselle.management.internal.notification.qualifier.Generic;
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
 * <p>Implementators of management protocols must observe the {@link NotificationEvent} event (using the {@link Observes} annotation), this way
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
	private Event<NotificationEvent> genericNotificationEvent;
	
	@Inject
	@AttributeChange
	private Event<NotificationEvent> attributeChangeNotificationEvent;
	
	/**
	 * Sends a generic notification to all management clients.
	 * 
	 * @param notification The notification to send
	 */
	public void sendNotification(Notification notification) {
		if (! AttributeChangeNotification.class.isInstance(notification) ){
			getGenericNotificationEvent().fire(new NotificationEvent(notification));
		}
		else{
			getAttributeChangeNotificationEvent().fire(new NotificationEvent(notification));
		}
	}

	@SuppressWarnings("unchecked")
	private Event<NotificationEvent> getGenericNotificationEvent() {
		if (genericNotificationEvent==null){
			genericNotificationEvent = Beans.getReference(Event.class , new AnnotationLiteral<Generic>() {});
		}
		
		return genericNotificationEvent;
	}
	
	@SuppressWarnings("unchecked")
	private Event<NotificationEvent> getAttributeChangeNotificationEvent() {
		if (attributeChangeNotificationEvent==null){
			attributeChangeNotificationEvent = Beans.getReference(Event.class , new AnnotationLiteral<AttributeChange>() {});
		}
		
		return attributeChangeNotificationEvent;
	}
	
	
	
}
