/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.model;

import java.util.Map;

import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationIgnore;
import org.demoiselle.jee.configuration.annotation.ConfigurationName;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;

/**
 * 
 * @author SERPRO
 *
 */
@Configuration(resource = "app", type = ConfigurationType.PROPERTIES, prefix = "")
public class ConfigModel {

    private Integer configInteger;
    private Short configShort;
    private Boolean configBoolean;
    private Byte configByte;
    private Character configCharacter;
    private Long configLong;
    private Double configDouble;
    private Float configFloat;

    private int configInt;

    private String configString;

    private Map<String, String> configMap;

    private String[] configArray;

    private Class<ConfigClassModel> configClassTyped;

    private ConfigEnum configEnum;

    @ConfigurationIgnore
    private String configFieldWithIgnore;

    @ConfigurationName("config-name-with-name")
    private String configStringWithName;

    @ConfigurationSuppressLogger
    private String configFieldWithSuppressLogger;

    public Integer getConfigInteger() {
        return configInteger;
    }

    public Short getConfigShort() {
        return configShort;
    }

    public Boolean getConfigBoolean() {
        return configBoolean;
    }

    public Byte getConfigByte() {
        return configByte;
    }

    public Character getConfigCharacter() {
        return configCharacter;
    }

    public Long getConfigLong() {
        return configLong;
    }

    public Double getConfigDouble() {
        return configDouble;
    }

    public Float getConfigFloat() {
        return configFloat;
    }

    public int getConfigInt() {
        return configInt;
    }

    public String getConfigString() {
        return configString;
    }

    public Map<?, ?> getConfigMap() {
        return configMap;
    }

    public String getConfigStringWithName() {
        return configStringWithName;
    }

    public ConfigEnum getConfigEnum() {
        return configEnum;
    }

    public String[] getConfigArray() {
        return configArray;
    }

    public Class<ConfigClassModel> getConfigClassTyped() {
        return configClassTyped;
    }

    public String getConfigFieldWithIgnore() {
        return configFieldWithIgnore;
    }

    public String getConfigFieldWithSuppressLogger() {
        return configFieldWithSuppressLogger;
    }

}
