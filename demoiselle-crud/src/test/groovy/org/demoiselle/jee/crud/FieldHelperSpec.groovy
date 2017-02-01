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

import org.demoiselle.jee.crud.field.FieldHelper
import org.demoiselle.jee.crud.field.FieldHelperMessage

import spock.lang.*

/**
 * @author SERPRO
 *
 */
class FieldHelperSpec extends Specification {

    ResourceInfo resourceInfo = Mock()
    UriInfo uriInfo = Mock()
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    FieldHelperMessage fieldHelperMessage = Mock()
    
    MultivaluedMap mvmRequest = new MultivaluedHashMap<>()
    
    FieldHelper fieldHelper = new FieldHelper(resourceInfo, uriInfo, drc, fieldHelperMessage);
    
    def "A request with 'fields' query string should populate 'DemoiselleRequestContext.fields'"(){
        given:
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        mvmRequest.addAll("fields", "id,name")
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        !drc.getFields().isEmpty()
        drc.getFields().contains("id")
        drc.getFields().contains("name")
        drc.getFields().size() == 2
    }
    
    def "A request with 'fields' query string and method annotated with @Search should be validated with @Search.fields property"(){
        
        given:
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields")
        
        def fields = UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields").getAnnotation(Search.class).fields() as List       
        def invalidFields = fields
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        invalidFields << "newInvalidField"
        
        mvmRequest.addAll("fields", invalidFields.join(","))
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        1 * fieldHelperMessage.filterFieldRequestNotExistsOnSearchField("newInvalidField")
        thrown(IllegalArgumentException)
        
    }
    
    def "A request with 'fields' query string and method that haven't @Search annotation should be executated" () {
        given:
        
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id", "name"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)
        !drc.getFields().isEmpty()
        drc.getFields().contains("id")
        drc.getFields().contains("name")
        drc.getFields().size() == 2
    }
       
    
}
