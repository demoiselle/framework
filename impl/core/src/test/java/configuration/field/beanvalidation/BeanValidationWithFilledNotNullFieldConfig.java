package configuration.field.beanvalidation;

import javax.validation.constraints.NotNull;

public class BeanValidationWithFilledNotNullFieldConfig {

	@NotNull
	private String stringAttributeNotNull;
	
	@NotNull
	private Integer intAttibuteNotNull;
	
	public String getStringAttributeNotNull() {
		return stringAttributeNotNull;
	}
	
	public Integer getIntAttibuteNotNull() {
		return intAttibuteNotNull;
	}
}
