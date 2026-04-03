/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import jakarta.enterprise.context.ApplicationScoped;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de recursos MCP.
 *
 * <p>Populado pela {@code McpBootstrapExtension} durante a inicialização do
 * container CDI. Armazena {@link ResourceDescriptor} indexados pela URI do
 * recurso e fornece consultas thread-safe.</p>
 *
 * @see ResourceDescriptor
 */
@ApplicationScoped
public class McpResourceRegistry {

    private final Map<String, ResourceDescriptor> resources = new ConcurrentHashMap<>();

    /**
     * Registra um recurso no registro.
     *
     * @param descriptor metadados do recurso a registrar
     * @throws IllegalStateException se já existir um recurso com a mesma URI
     * @throws NullPointerException  se {@code descriptor} ou sua URI for {@code null}
     */
    public void register(ResourceDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("ResourceDescriptor must not be null");
        }
        if (descriptor.uri() == null) {
            throw new NullPointerException("ResourceDescriptor uri must not be null");
        }
        ResourceDescriptor previous = resources.putIfAbsent(descriptor.uri(), descriptor);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate MCP resource URI '" + descriptor.uri()
                            + "': already registered by " + previous.method());
        }
    }

    /**
     * Busca um recurso pela URI.
     *
     * @param uri URI do recurso
     * @return {@link Optional} contendo o descritor, ou vazio se não encontrado
     */
    public Optional<ResourceDescriptor> findByUri(String uri) {
        return Optional.ofNullable(resources.get(uri));
    }

    /**
     * Retorna todos os recursos registrados como lista imutável.
     *
     * @return lista não-modificável de descritores
     */
    public List<ResourceDescriptor> listAll() {
        return Collections.unmodifiableList(List.copyOf(resources.values()));
    }

    /**
     * Retorna o número de recursos registrados.
     *
     * @return quantidade de recursos
     */
    public int size() {
        return resources.size();
    }
}
