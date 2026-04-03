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
 * Declara um método CDI como prompt MCP (Model Context Protocol).
 *
 * <p>Métodos anotados com {@code @McpPrompt} são descobertos automaticamente
 * pela {@code McpBootstrapExtension} durante a inicialização do container CDI
 * e registrados no {@code McpPromptRegistry}.</p>
 *
 * <p>Prompts MCP permitem que clientes descubram e utilizem templates de prompt
 * pré-definidos. Os argumentos do prompt são gerados automaticamente a partir
 * dos parâmetros do método, utilizando {@link McpParam} para descrição e
 * obrigatoriedade.</p>
 *
 * @see McpParam
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface McpPrompt {

    /**
     * Nome do prompt. Se não definido, o nome do método Java será utilizado.
     *
     * @return nome do prompt
     */
    String name() default "";

    /**
     * Descrição do prompt.
     *
     * @return descrição do prompt
     */
    String description() default "";
}
