package br.gov.frameworkdemoiselle.management.notification;

/**
 * 
 * Notification that can be sent by the {@link NotificationManager}.
 * 
 * @author serpro
 *
 */
public class Notification {
	
	private Object message;
	
	public Notification(){
	}
	
	public Notification(Object message) {
		super();
		this.message = message;
	}


	public Object getMessage() {
		return message;
	}

	
	public void setMessage(Object message) {
		this.message = message;
	}

	
	public Class<? extends Object> getType() {
		if (message!=null){
			return message.getClass();
		}
		
		return null;
	}

}
