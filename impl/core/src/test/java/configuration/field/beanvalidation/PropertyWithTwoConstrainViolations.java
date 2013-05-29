package configuration.field.beanvalidation;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.PROPERTIES;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "two-constrain-validation", type = PROPERTIES)
public class PropertyWithTwoConstrainViolations {

	@Max(10)
	@Min(50)
	private int attributeViolateTwoConstraints;

	public int getAttributeWithTwoConstrainValidations() {
		return attributeViolateTwoConstraints;
	}
}
