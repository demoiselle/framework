/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

import java.util.Objects;

/**
 * Mensagem de exceção REST imutável.
 *
 * @param error identificador do erro
 * @param errorDescription descrição do erro
 * @param errorLink link para documentação do erro (pode ser null)
 *
 * @author SERPRO
 */
public record DemoiselleRestExceptionMessage(
    String error,
    String errorDescription,
    String errorLink
) {
    public DemoiselleRestExceptionMessage {
        Objects.requireNonNull(error, "error não pode ser nulo");
    }
}
