package org.demoiselle.jee.configuration.model;

import java.util.Set;

import org.demoiselle.jee.configuration.annotation.Configuration;

@Configuration(resource = "app-test", prefix = "")
public class ConfigWithoutExtractorModel {
	
	private Set<?> field;

	public Set<?> getField() {
		return field;
	}
	
}
