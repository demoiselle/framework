/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Metadados de parâmetro para métodos {@link McpTool} e {@link McpPrompt}.
 *
 * <p>Permite personalizar o nome, descrição e obrigatoriedade de um parâmetro
 * no JSON Schema gerado automaticamente. Parâmetros sem esta anotação são
 * tratados como obrigatórios, com o nome do parâmetro Java.</p>
 *
 * @see McpTool
 * @see McpPrompt
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface McpParam {

    /**
     * Nome do parâmetro no JSON Schema. Se não definido, o nome do parâmetro Java será utilizado.
     *
     * @return nome do parâmetro
     */
    String name() default "";

    /**
     * Descrição do parâmetro.
     *
     * @return descrição do parâmetro
     */
    String description() default "";

    /**
     * Indica se o parâmetro é obrigatório.
     *
     * @return {@code true} se obrigatório (padrão), {@code false} caso contrário
     */
    boolean required() default true;
}
