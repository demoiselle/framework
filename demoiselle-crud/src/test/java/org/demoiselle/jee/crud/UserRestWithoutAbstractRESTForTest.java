/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.ws.rs.GET;

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
    public Result findWithException() {
        throw null;
    }
    
}
