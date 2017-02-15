/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

import org.demoiselle.jee.crud.TreeNodeField
import org.demoiselle.jee.crud.filter.FilterHelper
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig

import spock.lang.*

/**
 * Test of {@link FilterHelper} class.
 * 
 * @author SERPRO
 */
class FilterHelperSpec extends Specification {
    
    ContainerRequestContext requestContext = Mock()
    ResourceInfo resourceInfo = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    UriInfo uriInfo = Mock()
    CrudMessage crudMessage = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    PaginationHelperConfig dpc = Mock()
    
    FilterHelper filterHelper = new FilterHelper(resourceInfo, uriInfo, drc, crudMessage)
    
    def "A request with filter should populate 'DemoiselleRequestContext.filters'"(){
        
        given:
        dpc.getDefaultPagination() >> 50
        mvmRequest.addAll("mail", ["john@test.com", "john2@test.com", "john3@test.com"])
        mvmRequest.putSingle("name", "john john")
        mvmRequest.putSingle("range", "10-20")

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        filterHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.filters.containsKey("mail")
        TreeNodeField<String, Set<String>> mailNode = drc.filters.getChildByKey("mail")
        
        mailNode.getValue() == ["john@test.com", "john2@test.com", "john3@test.com"].toSet()
        
        drc.filters.containsKey("name")
        TreeNodeField<String, Set<String>> nameNode = drc.filters.getChildByKey("name")
        nameNode.getValue() == ["john john"].toSet()
        
        !drc.filters.containsKey("range")
        
    }
    
    def "A request with filter parameter and the target method annotated with @Search should validate the fields values"(){
        
        given:
        dpc.getDefaultPagination() >> 50
        mvmRequest.putSingle("name", "john john")
        mvmRequest.put("mail", ["john@test.com", "john2@test.com", "john3@test.com"])

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        filterHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(RuntimeException)
    }
    
    def "A request with filter parameter that has subfield and the target method annotated with @Search should be validade"() {
        given:
        dpc.getDefaultPagination() >> 50
        mvmRequest.putSingle("name", "john john")
        mvmRequest.putSingle("address(street,invalidField)", "my street")

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        filterHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * crudMessage.fieldRequestDoesNotExistsOnObject('invalidField', 'org.demoiselle.jee.crud.entity.AddressModelForTest')
    }

}
