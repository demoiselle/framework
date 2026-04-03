/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import java.util.List;
import java.util.Map;

/**
 * Interface para formatação de erros em respostas MCP.
 *
 * <p>Permite degradação graceful: quando {@code demoiselle-rest} está no classpath,
 * uma implementação baseada em ProblemDetail (RFC 9457) é utilizada; caso contrário,
 * a implementação padrão {@link PlainTextErrorFormatter} retorna texto simples.</p>
 */
public interface ErrorFormatter {

    /**
     * Formata uma exceção como conteúdo de resposta MCP.
     *
     * @param exception a exceção a ser formatada
     * @return lista de mapas representando o conteúdo de erro no formato MCP
     */
    List<Map<String, Object>> formatError(Throwable exception);
}
