package br.gov.frameworkdemoiselle.configuration.fields.basic;

public abstract class AbstractBasicFieldsConfig {

	private int primitiveInteger;

	private Integer wrappedInteger;

	private String stringWithSpace;

	private String stringWithComma;

	public Integer getWrappedInteger() {
		return wrappedInteger;
	}

	public void setWrappedInteger(Integer wrappedInteger) {
		this.wrappedInteger = wrappedInteger;
	}

	public int getPrimitiveInteger() {
		return primitiveInteger;
	}

	public void setPrimitiveInteger(int primitiveInteger) {
		this.primitiveInteger = primitiveInteger;
	}

	public String getStringWithSpace() {
		return stringWithSpace;
	}

	public void setStringWithSpace(String stringWithSpace) {
		this.stringWithSpace = stringWithSpace;
	}

	public String getStringWithComma() {
		return stringWithComma;
	}

	public void setStringWithComma(String stringWithComma) {
		this.stringWithComma = stringWithComma;
	}
}
