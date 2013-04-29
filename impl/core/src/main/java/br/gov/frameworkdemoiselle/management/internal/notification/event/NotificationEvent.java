package br.gov.frameworkdemoiselle.management.internal.notification.event;

import br.gov.frameworkdemoiselle.management.notification.Notification;
import br.gov.frameworkdemoiselle.management.notification.NotificationManager;

/**
 * Event fired when a notification is sent by {@link NotificationManager}.
 * Implementators can capture this event and by notified when the {@link NotificationManager}
 * sends notifications, so they can pass the notification to a underlying protocol such as JMX.
 * 
 * @author serpro
 *
 */
public class NotificationEvent {
	
	private Notification notification;
	
	public NotificationEvent(Notification notification){
		this.notification = notification;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}
}
