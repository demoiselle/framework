package configuration.prefix;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.XML;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(type = XML, prefix = "prefix")
public class XMLPrefixEndingWithoutDot extends AbstractPrefixConfig {
}
