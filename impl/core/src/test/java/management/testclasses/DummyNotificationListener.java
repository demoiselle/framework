package management.testclasses;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import br.gov.frameworkdemoiselle.management.internal.notification.event.NotificationEvent;
import br.gov.frameworkdemoiselle.management.internal.notification.qualifier.AttributeChange;
import br.gov.frameworkdemoiselle.management.internal.notification.qualifier.Generic;
import br.gov.frameworkdemoiselle.management.notification.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.notification.NotificationManager;

/**
 * Dummy class to test receiving of notifications sent by the {@link NotificationManager} 
 * 
 * @author serpro
 *
 */
@ApplicationScoped
public class DummyNotificationListener {
	
	private String message = null;
	
	public void listenNotification(@Observes @Generic NotificationEvent event){
		message = event.getNotification().getMessage().toString();
	}
	
	public void listenAttributeChangeNotification(@Observes @AttributeChange NotificationEvent event){
		AttributeChangeNotification notification = (AttributeChangeNotification)event.getNotification();
		message = notification.getMessage().toString() + " - " + notification.getAttributeName();
	}
	
	public String getMessage() {
		return message;
	}
}
