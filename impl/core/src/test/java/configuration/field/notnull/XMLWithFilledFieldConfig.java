package configuration.field.notnull;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "demoiselle", type = XML)
public class XMLWithFilledFieldConfig extends AbstractNotNullFieldConfig {
}
