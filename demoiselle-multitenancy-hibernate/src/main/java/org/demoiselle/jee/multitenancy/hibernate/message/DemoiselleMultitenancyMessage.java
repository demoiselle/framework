/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
public interface DemoiselleMultitenancyMessage {

	/**
	 * 
	 * @param tenant
	 *            Tenant name
	 * @return Error when set schema
	 */
	@MessageTemplate("{error-set-schema}")
	String errorSetSchema(String tenant);

	/**
	 * 
	 * @param tenant
	 *            Tenant name
	 * @return Erro then user does not belong to tenant
	 */
	@MessageTemplate("{error-user-not-belong-tenant}")
	String errorUserNotBelongTenant(String tenant);

	/**
	 * 
	 * @param tenant
	 *            Tenant name
	 * @param uri
	 *            Final URI
	 * @return Log message for CHANGED URL
	 */
	@MessageTemplate("{log-uri-path-changed}")
	String logUriPathChanged(String tenant, String uri);

	/**
	 * 
	 * @param uri
	 *            Final URI
	 * @return Log message for UNCHANGED URL
	 */
	@MessageTemplate("{log-uri-path-unchaged}")
	String logUriPathUnchanged(String uri);
}
