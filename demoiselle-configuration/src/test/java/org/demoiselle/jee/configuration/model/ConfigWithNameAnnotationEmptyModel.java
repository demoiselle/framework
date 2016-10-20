package org.demoiselle.jee.configuration.model;

import org.demoiselle.jee.core.annotation.Name;

public class ConfigWithNameAnnotationEmptyModel {
	
	@Name
	public String configString;

	public String getConfigString() {
		return configString;
	}

}
