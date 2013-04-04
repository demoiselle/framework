package br.gov.frameworkdemoiselle.configuration.field.notnull;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.PROPERTIES;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "without-field", type = PROPERTIES)
public class PropertyWithoutNotNullField extends AbstractNotNullFieldConfig {
}
