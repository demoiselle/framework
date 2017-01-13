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
    ContainerResponseContext responseContext = Mock()
    ResourceInfo info = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    MultivaluedMap mvmResponse = new MultivaluedHashMap()
    UriInfo uriInfo = Mock()
    DemoisellePaginationMessage message = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    CrudFilter crudFilter = new CrudFilter(uriInfo, info, drc, dpc, message)
    
    def "A request with filter shoud populate 'DemoiselleRequestContext.fieldsFilter'"(){
        
        given:
        dpc.getDefaultPagination() >> 50
        mvmRequest.putSingle("range", "10-20")
        mvmRequest.putSingle("type", "one")
        mvmRequest.put("days", ["sunday", "friday", "monday"])
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        drc.fieldsFilter.containsKey("type")
        drc.fieldsFilter.containsKey("days")
        !drc.fieldsFilter.containsKey("range")
        
        drc.fieldsFilter.get("type") == ["one"].toSet()
        drc.fieldsFilter.get("days") == ["sunday", "friday", "monday"].toSet()
    }

}
