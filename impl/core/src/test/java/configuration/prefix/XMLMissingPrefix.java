package configuration.prefix;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(type = XML, prefix = "missing.prefix")
public class XMLMissingPrefix extends AbstractPrefixConfig {
}
