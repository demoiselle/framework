/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

/**
 * Implementação padrão de {@link ErrorFormatter} que retorna a mensagem
 * da exceção como texto simples.
 *
 * <p>Usada quando {@code demoiselle-rest} não está no classpath.</p>
 */
@ApplicationScoped
public class PlainTextErrorFormatter implements ErrorFormatter {

    @Override
    public List<Map<String, Object>> formatError(Throwable exception) {
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
        return List.of(Map.of(
                "type", "text",
                "text", message
        ));
    }
}
