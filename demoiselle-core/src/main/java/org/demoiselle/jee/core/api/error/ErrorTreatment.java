/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.error;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface ErrorTreatment {

	public Response getFormatedError(Throwable exception, HttpServletRequest request);

}
