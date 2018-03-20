/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.ws.rs.GET;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.entity.UserModelForTest;

/**
 * 
 * @author SERPRO
 *
 */
public class UserRestWithoutAnnotationForTest extends AbstractREST<UserModelForTest, Long> {

    @Override
    @GET
    public Result find() {
        return null;
    }
    
}
