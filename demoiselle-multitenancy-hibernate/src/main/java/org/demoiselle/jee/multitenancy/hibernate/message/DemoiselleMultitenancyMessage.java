/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * Class that contains the messages for multitenancy module.
 *
 * @author SERPRO
 *
 */
@MessageBundle
public interface DemoiselleMultitenancyMessage {

    /**
     * Message that occurs error when set schema
     *
     * @param tenant Tenant name
     * @return Error when set schema
     */
    @MessageTemplate("{error-set-schema}")
    String errorSetSchema(String tenant);

    /**
     * Message to return when user does not belongs to tenant in Filter.
     *
     * @param tenant Tenant name
     * @return Erro then user does not belong to tenant
     */
    @MessageTemplate("{error-user-not-belong-tenant}")
    String errorUserNotBelongTenant(String tenant);

    /**
     * Message to return when the original url was changed in Filter.
     *
     * @param tenant Tenant name
     * @param uri Final URI
     * @return Log message for CHANGED URL
     */
    @MessageTemplate("{log-uri-path-changed}")
    String logUriPathChanged(String tenant, String uri);

    /**
     * Message to return when the original url was not changed in Filter.
     *
     * @param uri Final URI
     * @return Log message for UNCHANGED URL
     */
    @MessageTemplate("{log-uri-path-unchaged}")
    String logUriPathUnchanged(String uri);

    /**
     * Error when in creation of Tenant occurs a drop tables error.
     *
     * @param database descrever
     * @return descrever
     */
    @MessageTemplate("{log-warn-error-when-drop-database}")
    String logWarnErrorWhenDropDatabase(String database);

    /**
     * Log warn when occurs error on load JSON configuration of Tenant.
     *
     * @return descrever
     */
    @MessageTemplate("{log-warn-error-when-parse-configuration-tenant}")
    String logWarnErrorWhenParseConfigurationTenant();

}
