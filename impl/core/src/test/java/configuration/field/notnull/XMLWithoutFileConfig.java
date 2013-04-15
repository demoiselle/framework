package configuration.field.notnull;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource = "nofile", type = XML)
public class XMLWithoutFileConfig extends AbstractNotNullFieldConfig {
}
