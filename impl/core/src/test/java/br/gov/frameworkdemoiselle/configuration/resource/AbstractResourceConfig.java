package br.gov.frameworkdemoiselle.configuration.resource;

public abstract class AbstractResourceConfig {

	private int primitiveInteger;

	private String string;

	public int getPrimitiveInteger() {
		return primitiveInteger;
	}

	public void setPrimitiveInteger(int primitiveInteger) {
		this.primitiveInteger = primitiveInteger;
	}

	public String getStringWithComma() {
		return string;
	}

	public void setStringWithComma(String stringWithComma) {
		this.string = stringWithComma;
	}
}