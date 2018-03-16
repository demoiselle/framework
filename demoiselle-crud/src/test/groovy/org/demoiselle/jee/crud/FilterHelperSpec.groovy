/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.crud.TreeNodeField
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.filter.FilterHelper
import spock.lang.Specification

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

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
    DemoiselleCrudConfig crudConfig= new DemoiselleCrudConfig(true, true, true, true, 50)
    
    FilterHelper filterHelper = new FilterHelper(resourceInfo, crudConfig, uriInfo, drc, crudMessage)
    
    def "A request with filter should populate 'DemoiselleRequestContext.filterContext.filters'"(){
        
        given:
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
        drc.getFilterContext().getFilters().containsKey("mail")
        TreeNodeField<String, Set<String>> mailNode = drc.getFilterContext().getFilters().getChildByKey("mail")
        
        mailNode.getValue() == ["john@test.com", "john2@test.com", "john3@test.com"].toSet()

        drc.getFilterContext().getFilters().containsKey("name")
        TreeNodeField<String, Set<String>> nameNode = drc.getFilterContext().getFilters().getChildByKey("name")
        nameNode.getValue() == ["john john"].toSet()
        
        !drc.getFilterContext().getFilters().containsKey("range")
        
    }

//    TODO: Este teste deve ser feito no DemoiselleCrudHelper
//    def "A request with filter parameter and the target method annotated with @Search should validate the fields values"(){
//
//        given:
//        mvmRequest.putSingle("name", "john john")
//        mvmRequest.put("mail", ["john@test.com", "john2@test.com", "john3@test.com"])
//
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        when:
//        filterHelper.execute(resourceInfo, uriInfo)
//
//        then:
//        thrown(RuntimeException)
//    }
//
//    def "A request with filter parameter that has subfield and the target method annotated with @Search should be validade"() {
//        given:
//        mvmRequest.putSingle("name", "john john")
//        mvmRequest.putSingle("address(street,invalidField)", "my street")
//
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        when:
//        filterHelper.execute(resourceInfo, uriInfo)
//
//        then:
//        thrown(IllegalArgumentException)
//        1 * crudMessage.fieldRequestDoesNotExistsOnObject('invalidField', 'org.demoiselle.jee.crud.entity.AddressModelForTest')
//    }

}
