/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.message;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;

/**
 * Message bundle for CRUD module error messages.
 * Replaces hardcoded Portuguese strings in AbstractDAO and related classes.
 *
 * @author SERPRO
 */
@MessageBundle
public interface DemoiselleCrudMessage {

    @MessageTemplate("{persist-error}")
    String persistError();

    @MessageTemplate("{merge-error}")
    String mergeError();

    @MessageTemplate("{remove-error}")
    String removeError();

    @MessageTemplate("{find-error}")
    String findError();

    @MessageTemplate("{enum-type-check-error}")
    String enumTypeCheckError();

    @MessageTemplate("{uuid-type-check-error}")
    String uuidTypeCheckError();

    @MessageTemplate("{enum-conversion-error}")
    String enumConversionError();

    @MessageTemplate("{enum-value-not-found}")
    String enumValueNotFound(String value);

}
