package br.gov.frameworkdemoiselle.configuration.field.array;

public abstract class AbstractArrayFieldConfig {

	private int[] primitiveIntegers;

	private Integer[] wrappedIntegers;

	private String[] strings;

	private double[] primitiveDoubles;

	private Double[] wrappedDoubles;

	public int[] getPrimitiveIntegers() {
		return primitiveIntegers;
	}

	public void setPrimitiveIntegers(int[] primitiveIntegers) {
		this.primitiveIntegers = primitiveIntegers;
	}

	public Integer[] getWrappedIntegers() {
		return wrappedIntegers;
	}

	public void setWrappedIntegers(Integer[] wrappedIntegers) {
		this.wrappedIntegers = wrappedIntegers;
	}

	public String[] getStrings() {
		return strings;
	}

	public void setStrings(String[] strings) {
		this.strings = strings;
	}

	public double[] getPrimitiveDoubles() {
		return primitiveDoubles;
	}

	public void setPrimitiveDoubles(double[] primitiveDoubles) {
		this.primitiveDoubles = primitiveDoubles;
	}

	public Double[] getWrappedDoubles() {
		return wrappedDoubles;
	}

	public void setWrappedDoubles(Double[] wrappedDoubles) {
		this.wrappedDoubles = wrappedDoubles;
	}
}
