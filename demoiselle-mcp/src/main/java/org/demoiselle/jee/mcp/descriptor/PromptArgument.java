/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

/**
 * Argumento esperado por um prompt MCP.
 *
 * <p>Cada {@code PromptArgument} descreve um parâmetro do método anotado
 * com {@code @McpPrompt}, incluindo nome, descrição e obrigatoriedade.</p>
 *
 * @param name        nome do argumento
 * @param description descrição do argumento
 * @param required    se o argumento é obrigatório
 */
public record PromptArgument(
    String name,
    String description,
    boolean required
) {}
