/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

import java.lang.reflect.Method;

/**
 * Metadados de um recurso MCP registrado.
 *
 * <p>Cada {@code ResourceDescriptor} é criado pela {@code McpBootstrapExtension}
 * durante a inicialização do container CDI e armazenado no {@code McpResourceRegistry}.</p>
 *
 * @param uri          URI do recurso (única no registro)
 * @param name         nome do recurso
 * @param description  descrição do recurso
 * @param mimeType     MIME type do conteúdo retornado
 * @param beanInstance instância do bean CDI que contém o método
 * @param method       referência ao método Java anotado com {@code @McpResource}
 */
public record ResourceDescriptor(
    String uri,
    String name,
    String description,
    String mimeType,
    Object beanInstance,
    Method method
) {}
