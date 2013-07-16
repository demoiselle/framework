package br.gov.frameworkdemoiselle.management;

/**
 * Event fired when a notification is sent by {@link NotificationManager}.
 * Implementators can capture this event and be notified when the {@link NotificationManager}
 * sends notifications, so they can pass the notification to the underlying technology.
 * 
 * @author serpro
 *
 */
public interface ManagementNotificationEvent {
	
	public GenericNotification getNotification();

}
