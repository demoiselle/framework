/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.demoiselle.jee.core.api.crud.Result;

/**
 * 
 * @author SERPRO
 *
 */
public class UserRestWithoutAbstractRESTForTest {

    @GET
    public Result find() {
        return null;
    }

    @GET
    public Response findWithException() {
        ErrorMsgForTest entity = new ErrorMsgForTest();
        return Response.status(400).entity(entity).type("application/json").build();
    }
    
}
