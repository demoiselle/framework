/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import jakarta.enterprise.context.ApplicationScoped;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de prompts MCP.
 *
 * <p>Populado pela {@code McpBootstrapExtension} durante a inicialização do
 * container CDI. Armazena {@link PromptDescriptor} indexados pelo nome do
 * prompt e fornece consultas thread-safe.</p>
 *
 * @see PromptDescriptor
 */
@ApplicationScoped
public class McpPromptRegistry {

    private final Map<String, PromptDescriptor> prompts = new ConcurrentHashMap<>();

    /**
     * Registra um prompt no registro.
     *
     * @param descriptor metadados do prompt a registrar
     * @throws IllegalStateException se já existir um prompt com o mesmo nome
     * @throws NullPointerException  se {@code descriptor} ou seu nome for {@code null}
     */
    public void register(PromptDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("PromptDescriptor must not be null");
        }
        if (descriptor.name() == null) {
            throw new NullPointerException("PromptDescriptor name must not be null");
        }
        PromptDescriptor previous = prompts.putIfAbsent(descriptor.name(), descriptor);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate MCP prompt name '" + descriptor.name()
                            + "': already registered by " + previous.method());
        }
    }

    /**
     * Busca um prompt pelo nome.
     *
     * @param name nome do prompt
     * @return {@link Optional} contendo o descritor, ou vazio se não encontrado
     */
    public Optional<PromptDescriptor> find(String name) {
        return Optional.ofNullable(prompts.get(name));
    }

    /**
     * Retorna todos os prompts registrados como lista imutável.
     *
     * @return lista não-modificável de descritores
     */
    public List<PromptDescriptor> listAll() {
        return Collections.unmodifiableList(List.copyOf(prompts.values()));
    }

    /**
     * Retorna o número de prompts registrados.
     *
     * @return quantidade de prompts
     */
    public int size() {
        return prompts.size();
    }
}
