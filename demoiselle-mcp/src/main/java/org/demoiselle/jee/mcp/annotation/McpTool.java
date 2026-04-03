/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declara um método CDI como ferramenta MCP (Model Context Protocol).
 *
 * <p>Métodos anotados com {@code @McpTool} são descobertos automaticamente
 * pela {@code McpBootstrapExtension} durante a inicialização do container CDI
 * e registrados no {@code McpToolRegistry}.</p>
 *
 * <p>O {@code inputSchema} (JSON Schema) da ferramenta é gerado automaticamente
 * a partir dos parâmetros do método. Use {@link McpParam} para personalizar
 * descrição, obrigatoriedade e nome dos parâmetros.</p>
 *
 * @see McpParam
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface McpTool {

    /**
     * Nome da ferramenta MCP. Se não definido, o nome do método Java será utilizado.
     *
     * @return nome da ferramenta
     */
    String name() default "";

    /**
     * Descrição da ferramenta (obrigatório para MCP).
     *
     * @return descrição da ferramenta
     */
    String description();
}
