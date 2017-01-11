/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * This interface defines a method to treat @Exception classes in Framework.
 * 
 * @author SERPRO
 *
 */
public interface ExceptionTreatment {

	public Response getFormatedError(Throwable exception, HttpServletRequest request);

}
