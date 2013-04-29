package br.gov.frameworkdemoiselle.management.internal.validators;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues;


public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Object> {

	private br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues.ValueType valueType;
	private String[] allowedValues;

	@Override
	public void initialize(AllowedValues constraintAnnotation) {
		valueType = constraintAnnotation.valueType();
		allowedValues = constraintAnnotation.allows();
	}
	
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		
		if (value==null){
			return false;
		}

		switch(valueType){
			case STRING:
				for (String str : allowedValues){
					if (str.equals(value)) return true;
				}
				return false;
				
			case INTEGER:
				try{
					Integer number = Integer.valueOf(value.toString());
					String strNumber = number.toString();
					for (String str : allowedValues){
						if (str.equals(strNumber)) return true;
					}

					return false;
				}
				catch(NumberFormatException ne){
					return false;
				}
				
			case DECIMAL:
				try{
					BigDecimal number = new BigDecimal(value.toString());
					String strNumber = number.toString();
					for (String str : allowedValues){
						if (str.equals(strNumber)) return true;
					}
				}
				catch(NumberFormatException ne){
					return false;
				}
				
				return false;
		}

		return false;
	}

}
