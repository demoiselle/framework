package org.demoiselle.jee.persistence.crud.manager;

public class CrudContainer {
	
	private String path;
	private Class<?> model;
	private Class<?> restClass;
	private Class<?> businessClass;
	private Class<?> persistenceClass;
	
	public Class<?> getModel() {
		return model;
	}

	public void setModel(Class<?> model) {
		this.model = model;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Class<?> getBusinessClass() {
		return businessClass;
	}
	public void setBusinessClass(Class<?> businessClass) {
		this.businessClass = businessClass;
	}
	public Class<?> getPersistenceClass() {
		return persistenceClass;
	}
	public void setPersistenceClass(Class<?> persistenceClass) {
		this.persistenceClass = persistenceClass;
	}
	public Class<?> getRestClass() {
		return restClass;
	}
	public void setRestClass(Class<?> restClass) {
		this.restClass = restClass;
	}
	
	

}
