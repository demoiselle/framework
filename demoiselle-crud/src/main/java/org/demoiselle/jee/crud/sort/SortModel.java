/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import java.util.Objects;

/**
 * Modelo imutável para ordenação de consultas CRUD.
 *
 * @param type direção da ordenação (ASC ou DESC)
 * @param field nome do campo para ordenação
 *
 * @author SERPRO
 */
public record SortModel(CrudSort type, String field) {

    public SortModel {
        Objects.requireNonNull(type, "tipo de ordenação não pode ser nulo");
        Objects.requireNonNull(field, "campo de ordenação não pode ser nulo");
        if (field.isBlank()) {
            throw new IllegalArgumentException("campo de ordenação não pode ser vazio");
        }
    }
}
