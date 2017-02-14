/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

import org.demoiselle.jee.crud.sort.CrudSort
import org.demoiselle.jee.crud.sort.SortHelper
import org.demoiselle.jee.crud.sort.SortHelperMessage

import spock.lang.*

/**
 * Test of {@link SortHelper} class.
 * 
 * @author SERPRO
 */
class SortHelperSpec extends Specification {

    ResourceInfo resourceInfo = Mock()
    UriInfo uriInfo = Mock()
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    SortHelperMessage message = Mock()
    CrudMessage crudMessage = Mock()
    
    MultivaluedMap mvmRequest = new MultivaluedHashMap<>()
    
    SortHelper sortHelper = new SortHelper(resourceInfo, uriInfo, drc, message, crudMessage)
    
    def "A request with 'sort' query string should populate 'DemoiselleRequestContext.sorts'"(){
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id", "name"])
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.getSorts() != null
        drc.getSorts().size() == 2
              
        drc.getSorts().get(0).getField() == "id"
        drc.getSorts().get(0).getType() == CrudSort.ASC
        
        drc.getSorts().get(1).getField() == "name"
        drc.getSorts().get(1).getType() == CrudSort.ASC
    }
    
    def "A request with 'sort' query string and 'desc' without parameters should add all parameters as DESC order"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id", "name"])
        mvmRequest.putSingle("desc", "")
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.getSorts() != null
        drc.getSorts().size() == 2
        
        drc.getSorts().get(0).getField() == "id"
        drc.getSorts().get(0).getType() == CrudSort.DESC
        
        drc.getSorts().get(1).getField() == "name"
        drc.getSorts().get(1).getType() == CrudSort.DESC
        
    }
    
    def "A request with 'sort' query string and 'desc' with parameters should separate parameters as DESC and ASC order"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id", "name"])
        mvmRequest.putSingle("desc", "name")
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.getSorts() != null
        drc.getSorts().size() == 2
        drc.getSorts().get(0).getField() == "id"
        drc.getSorts().get(0).getType() == CrudSort.ASC

        drc.getSorts().get(1).getField() == "name"
        drc.getSorts().get(1).getType() == CrudSort.DESC
    }
    
    def "A request with 'sort' and 'desc' query string parameters should respect the order"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id", "name", "mail"])
        mvmRequest.addAll("desc", ["name", "mail"])
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.getSorts() != null
        
        drc.getSorts().get(0).getType() == CrudSort.ASC
        drc.getSorts().get(0).getField() == "id"
        
        drc.getSorts().get(1).getType() == CrudSort.DESC
        drc.getSorts().get(1).getField() == "name"
        
        drc.getSorts().get(2).getType() == CrudSort.DESC
        drc.getSorts().get(2).getField() == "mail"
    }
    
    def "A request without 'sort' and 'desc' query string parameters should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        mvmRequest.addAll("fields", ["id", "name", "mail"])
        mvmRequest.putSingle("desc", "name")
        uriInfo.getRequestUri() >> uri
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * message.descParameterWithoutSortParameter()
    }
    
    def "A request with 'sort' parameter and a invalid value should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        mvmRequest.addAll("sort", ["id", "name", "invalidField"])
        uriInfo.getRequestUri() >> uri
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "A request with 'sort' parameter and a value that doesn't match @Search.fields should throw RuntimeException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        mvmRequest.addAll("sort", ["id", "name", "invalidField"])
        uriInfo.getRequestUri() >> uri
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(RuntimeException)
        1 * crudMessage.fieldRequestDoesNotExistsOnSearchField('invalidField')
    }
    
    def "A request with 'sort' parameters and a 'desc' parameter that doesn't match the 'sort' parameter shold throw RuntimeException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        
        mvmRequest.addAll("sort", ["id", "name"])
        mvmRequest.putSingle("desc", "id1")
        uriInfo.getRequestUri() >> uri
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        sortHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(RuntimeException)
        1 * crudMessage.fieldRequestDoesNotExistsOnSearchField('id1')
    }
    
}
