/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Metadados de um prompt MCP registrado.
 *
 * <p>Cada {@code PromptDescriptor} é criado pela {@code McpBootstrapExtension}
 * durante a inicialização do container CDI e armazenado no {@code McpPromptRegistry}.</p>
 *
 * @param name         nome do prompt (único no registro)
 * @param description  descrição do prompt
 * @param arguments    lista de argumentos esperados pelo prompt
 * @param beanInstance instância do bean CDI que contém o método
 * @param method       referência ao método Java anotado com {@code @McpPrompt}
 */
public record PromptDescriptor(
    String name,
    String description,
    List<PromptArgument> arguments,
    Object beanInstance,
    Method method
) {}
