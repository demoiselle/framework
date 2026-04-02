/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.AbstractREST;
import org.demoiselle.jee.crud.cache.Cacheable;
import org.demoiselle.jee.crud.entity.UserModelForTest;

import jakarta.ws.rs.GET;

/**
 * 
 * @author SERPRO
 *
 */
public class UserRestForTest extends AbstractREST<UserModelForTest, Long> {

    @Override
    @GET
    public Result find() {
        return null;
    }

    @GET
    @Search(fields={"name", "address"}, quantityPerPage = 10, withPagination = true)
    public Result findWithSearch() {
        return null;
    }
    
    @GET
    @Search(fields={"name"}, withPagination = false)
    public Result findWithSearchAnnotationAndPaginationDisabled(){
        return null;
    }
    
    @GET
    @Search(fields={"id", "name", "mail"})
    public Result findWithSearchAndFields(){
        return null;
    }
    
    @GET
    @Search(fields={"id", "name", "address(street)"})
    public Result findWithSearchAndFieldsWithSubFields(){
        return null;
    }
    
    @GET
    @Search(fields={"*"})
    public Result findWithSearchAndAllFields(){
        return null;
    }

    @GET
    @Cacheable(ttl = 60)
    @Search(fields={"id", "name"}, quantityPerPage = 10, withPagination = true)
    public Result findWithCacheable() {
        return null;
    }
    
}
