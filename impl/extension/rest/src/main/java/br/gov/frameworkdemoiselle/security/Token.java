package br.gov.frameworkdemoiselle.security;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Token {

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEmpty() {
		return this.value == null;
	}

	public void clear() {
		this.value = null;
	}
}
