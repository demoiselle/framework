package br.gov.frameworkdemoiselle.configuration.resource;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.PROPERTIES;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(resource="nofile", type = PROPERTIES)
public class PropertiesWithoutFileConfig extends AbstractResourceConfig{

}
