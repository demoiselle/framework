package br.gov.frameworkdemoiselle.security;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.util.Strings;

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
		return Strings.isEmpty(value);
	}
}
