/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import javax.ws.rs.BadRequestException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.filter.FilterHelper
import org.demoiselle.jee.crud.filter.FilterHelperMessage
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig
import org.demoiselle.jee.crud.pagination.DemoisellePaginationMessage
import org.demoiselle.jee.crud.pagination.PaginationHelper
import org.demoiselle.jee.crud.pagination.ResultSet
import org.demoiselle.jee.crud.sort.SortHelper

import spock.lang.*

/**
 * @author SERPRO
 *
 */
class CrudFilterSpec extends Specification{
    
    ContainerRequestContext requestContext = Mock()
    ContainerResponseContext responseContext = Mock()
    ResourceInfo resourceInfo = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    MultivaluedMap mvmResponse = new MultivaluedHashMap()
    DemoisellePaginationMessage message = Mock()
    FilterHelperMessage filterHelperMessage = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoisellePaginationConfig dpc = Mock()
    
    UriInfo uriInfo = Mock()
    
    SortHelper sortHelper = new SortHelper(resourceInfo, uriInfo, drc)
    PaginationHelper paginationHelper = new PaginationHelper(resourceInfo, uriInfo, dpc, drc)
    FilterHelper filterHelper = new FilterHelper(resourceInfo, uriInfo, drc, filterHelperMessage)
    //FieldHelper fieldHelper = new FieldHelper()
    CrudFilter crudFilter = new CrudFilter(resourceInfo, uriInfo, drc, paginationHelper, sortHelper, filterHelper)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        dpc.getDefaultPagination() >> 20
        dpc.getIsGlobalEnabled() >> true
        mvmRequest.putSingle("range", "10-20")
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        drc.offset == 10
        drc.limit == 20
        notThrown(BadRequestException)
        
    }
    
    private configureRequestForCrud(){
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> AbstractREST.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
    }
}
