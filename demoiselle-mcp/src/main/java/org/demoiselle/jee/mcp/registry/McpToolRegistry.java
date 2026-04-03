/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import jakarta.enterprise.context.ApplicationScoped;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de ferramentas MCP.
 *
 * <p>Populado pela {@code McpBootstrapExtension} durante a inicialização do
 * container CDI. Armazena {@link ToolDescriptor} indexados pelo nome da
 * ferramenta e fornece consultas thread-safe.</p>
 *
 * @see ToolDescriptor
 */
@ApplicationScoped
public class McpToolRegistry {

    private final Map<String, ToolDescriptor> tools = new ConcurrentHashMap<>();

    /**
     * Registra uma ferramenta no registro.
     *
     * @param descriptor metadados da ferramenta a registrar
     * @throws IllegalStateException se já existir uma ferramenta com o mesmo nome
     * @throws NullPointerException  se {@code descriptor} ou seu nome for {@code null}
     */
    public void register(ToolDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("ToolDescriptor must not be null");
        }
        if (descriptor.name() == null) {
            throw new NullPointerException("ToolDescriptor name must not be null");
        }
        ToolDescriptor previous = tools.putIfAbsent(descriptor.name(), descriptor);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate MCP tool name '" + descriptor.name()
                            + "': already registered by " + previous.method());
        }
    }

    /**
     * Busca uma ferramenta pelo nome.
     *
     * @param name nome da ferramenta
     * @return {@link Optional} contendo o descritor, ou vazio se não encontrado
     */
    public Optional<ToolDescriptor> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Retorna todas as ferramentas registradas como lista imutável.
     *
     * @return lista não-modificável de descritores
     */
    public List<ToolDescriptor> listAll() {
        return Collections.unmodifiableList(List.copyOf(tools.values()));
    }

    /**
     * Retorna o número de ferramentas registradas.
     *
     * @return quantidade de ferramentas
     */
    public int size() {
        return tools.size();
    }

    /**
     * Remove do registro todas as ferramentas cujos nomes estejam no conjunto fornecido.
     *
     * @param disabledNames nomes das ferramentas a desabilitar
     */
    public void applyDisabledFilter(Set<String> disabledNames) {
        if (disabledNames == null || disabledNames.isEmpty()) {
            return;
        }
        disabledNames.forEach(tools::remove);
    }
}
