/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Metadados de uma ferramenta MCP registrada.
 *
 * <p>Cada {@code ToolDescriptor} é criado pela {@code McpBootstrapExtension}
 * durante a inicialização do container CDI e armazenado no {@code McpToolRegistry}.</p>
 *
 * @param name         nome da ferramenta (único no registro)
 * @param description  descrição da ferramenta
 * @param inputSchema  JSON Schema gerado a partir dos parâmetros do método
 * @param beanInstance instância do bean CDI que contém o método
 * @param method       referência ao método Java anotado com {@code @McpTool}
 */
public record ToolDescriptor(
    String name,
    String description,
    Map<String, Object> inputSchema,
    Object beanInstance,
    Method method
) {}
