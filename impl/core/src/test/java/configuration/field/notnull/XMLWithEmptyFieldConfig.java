package configuration.field.notnull;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "empty-field", type = XML)
public class XMLWithEmptyFieldConfig extends AbstractNotNullFieldConfig {
}
