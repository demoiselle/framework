package br.gov.frameworkdemoiselle.util;

import javax.enterprise.util.AnnotationLiteral;

import br.gov.frameworkdemoiselle.annotation.Name;

public class NameQualifier extends AnnotationLiteral<Name> implements Name {

	private static final long serialVersionUID = 1L;

	private final String value;

	public NameQualifier(String value) {
		this.value = value;
	}

	@Override
	public String value() {
		return this.value;
	}
}
