package br.gov.frameworkdemoiselle.management;

/**
 * Specialized message that can be used to create a {@link Notification} and be sent using {@link NotificationManager#sendNotification(Notification notification)}.
 * 
 * This message contains information about an attribute change.
 * 
 * @author serpro
 *
 */
public class AttributeChangeMessage {
	
	private String description;
	
	private String attributeName;

	private Class<? extends Object> attributeType;

	private Object oldValue;

	private Object newValue;
	
	public AttributeChangeMessage(){
	}
	
	public AttributeChangeMessage(String description, String attributeName, Class<? extends Object> attributeType,
			Object oldValue, Object newValue) {
		this.description = description;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAttributeName() {
		return attributeName;
	}

	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	
	public Class<? extends Object> getAttributeType() {
		return attributeType;
	}

	
	public void setAttributeType(Class<? extends Object> attributeType) {
		this.attributeType = attributeType;
	}

	
	public Object getOldValue() {
		return oldValue;
	}

	
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	
	public Object getNewValue() {
		return newValue;
	}

	
	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}
	
}
