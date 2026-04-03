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
 * Declara um método CDI como recurso MCP (Model Context Protocol).
 *
 * <p>Métodos anotados com {@code @McpResource} são descobertos automaticamente
 * pela {@code McpBootstrapExtension} durante a inicialização do container CDI
 * e registrados no {@code McpResourceRegistry}.</p>
 *
 * <p>Recursos MCP permitem que clientes descubram e leiam dados estruturados
 * como arquivos, registros de banco de dados e outros conteúdos.</p>
 *
 * @see McpParam
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface McpResource {

    /**
     * URI do recurso (obrigatório).
     *
     * @return URI do recurso
     */
    String uri();

    /**
     * Nome do recurso (obrigatório).
     *
     * @return nome do recurso
     */
    String name();

    /**
     * Descrição do recurso.
     *
     * @return descrição do recurso
     */
    String description() default "";

    /**
     * MIME type do conteúdo retornado pelo recurso.
     *
     * @return MIME type (padrão: {@code "text/plain"})
     */
    String mimeType() default "text/plain";
}
