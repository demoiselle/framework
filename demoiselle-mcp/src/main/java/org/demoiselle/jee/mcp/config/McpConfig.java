/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.config;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configuração do módulo MCP via demoiselle.properties.
 *
 * <p>Carrega propriedades com prefixo {@code demoiselle.mcp} quando executado
 * dentro de um container CDI com o módulo {@code demoiselle-configuration}.
 * Também funciona como POJO simples para testes unitários.</p>
 */
@Configuration(prefix = "demoiselle.mcp")
public class McpConfig {

    private static final Logger LOG = Logger.getLogger(McpConfig.class.getName());

    private static final Set<String> KNOWN_TRANSPORTS = Set.of("sse", "stdio");

    private String serverName = "demoiselle-mcp";
    private String serverVersion = "1.0.0";
    private String transport = "sse";
    private boolean securityEnabled = false;
    private String toolsDisabled = "";

    public McpConfig() {
    }

    public McpConfig(String serverName, String serverVersion) {
        this.serverName = serverName;
        this.serverVersion = serverVersion;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public String getToolsDisabled() {
        return toolsDisabled;
    }

    public void setToolsDisabled(String toolsDisabled) {
        this.toolsDisabled = toolsDisabled;
    }

    /**
     * Returns the effective transport, falling back to {@code "sse"} with a
     * warning when the configured value is not recognised.
     *
     * @return {@code "sse"} or {@code "stdio"}
     */
    public String getEffectiveTransport() {
        String value = (transport == null) ? "" : transport.trim().toLowerCase();
        if (KNOWN_TRANSPORTS.contains(value)) {
            return value;
        }
        LOG.warning(() -> "Unknown MCP transport '" + transport
                + "'; falling back to 'sse'. Valid values are: " + KNOWN_TRANSPORTS);
        return "sse";
    }

    /**
     * Returns the set of disabled tool names parsed from the comma-separated
     * {@code toolsDisabled} string.
     *
     * @return unmodifiable set of disabled tool names
     */
    public Set<String> getDisabledToolNames() {
        if (toolsDisabled == null || toolsDisabled.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(toolsDisabled.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
