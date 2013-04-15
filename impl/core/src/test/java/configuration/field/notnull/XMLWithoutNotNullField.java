package configuration.field.notnull;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "without-field", type = XML)
public class XMLWithoutNotNullField extends AbstractNotNullFieldConfig {
}
