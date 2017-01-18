/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig
import org.demoiselle.jee.crud.pagination.DemoisellePaginationMessage
import org.demoiselle.jee.crud.pagination.ResultSet

import spock.lang.Specification

/**
 * @author SERPRO
 *
 */
class FilterRequestSpec extends Specification {
    
    ContainerRequestContext requestContext = Mock()
    ResourceInfo info = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    DemoisellePaginationConfig dpc = Mock()
    
    CrudFilter crudFilter = new CrudFilter(uriInfo, info, drc, dpc, message)
    
    def "A request with filter shoud populate 'DemoiselleRequestContext.fieldsFilter'"(){
        
        given:
        dpc.getDefaultPagination() >> 50
        mvmRequest.putSingle("range", "10-20")
        mvmRequest.putSingle("name", "john john")
        mvmRequest.put("mail", ["john@test.com", "john2@test.com", "john3@test.com"])

        info.getResourceClass() >> UserRestForTest.class
        info.getResourceClass().getSuperclass() >> AbstractREST.class
        info.getResourceMethod() >> AbstractREST.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        drc.filters.containsKey("mail")
        drc.filters.containsKey("name")
        !drc.filters.containsKey("range")
        
        drc.filters.get("name") == ["john john"].toSet()
        drc.filters.get("mail") == ["john@test.com", "john2@test.com", "john3@test.com"].toSet()
    }

}
