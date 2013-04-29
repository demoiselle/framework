package br.gov.frameworkdemoiselle.management.notification;

/**
 * Special notification to denote an attribute has changed values.
 * 
 * @see Notification
 * 
 * @author serpro
 *
 */
public class AttributeChangeNotification extends Notification {
	
	private String attributeName;
	
	private Class<? extends Object> attributeType;
	
	private Object oldValue;
	
	private Object newValue;
	
	public AttributeChangeNotification(){}
	
	public AttributeChangeNotification(Object message, String attributeName, Class<? extends Object> attributeType, Object oldValue,
			Object newValue) {
		super(message);
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
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
