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
import org.demoiselle.jee.crud.TreeNodeField;

import spock.lang.*

/**
 * Test of {@link FieldHelper} class.
 * 
 * @author SERPRO
 */
class FieldHelperSpec extends Specification {

    ResourceInfo resourceInfo = Mock()
    UriInfo uriInfo = Mock()
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    FieldHelperMessage fieldHelperMessage = Mock()
    CrudMessage crudMessage = Mock()
    
    MultivaluedMap mvmRequest = new MultivaluedHashMap<>()
    
    FieldHelper fieldHelper = new FieldHelper(resourceInfo, uriInfo, drc, fieldHelperMessage, crudMessage);
    
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
        drc.getFields() != null
        drc.getFields().getChildren().get(0).getKey() == "id"
        drc.getFields().getChildren().get(1).getKey() == "name"
        drc.getFields().getChildren().size() == 2
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
        1 * crudMessage.fieldRequestDoesNotExistsOnSearchField("newInvalidField")
        thrown(IllegalArgumentException)
        
    }
    
    def "A request with 'fields' query string and method that haven't @Search annotation should be executated" () {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id"])
        mvmRequest.addAll("desc", ["id"])
        mvmRequest.putSingle("range", "0-9")
        mvmRequest.addAll("fields", ["id", "name"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)
        drc.getFields() != null
        drc.getFields().getChildren().get(0).getKey() == "id"
        drc.getFields().getChildren().get(1).getKey() == "name"
        drc.getFields().getChildren().size() == 2
    }
    
    def "A request with 'fields' query string can be fields with subfields like field(subField1,subField2,subField2(subSubField1))"(){
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("sort", ["id"])
        mvmRequest.addAll("desc", ["id"])
        mvmRequest.putSingle("range", "0-9")
        mvmRequest.addAll("fields", ["id", "name", "address(id,address,country(name))"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)
        drc.getFields() != null
        drc.getFields().getChildren().size() == 3
        
        drc.getFields().getChildren().get(0).getKey() == "id"
        drc.getFields().getChildren().get(1).getKey() == "name"
        
        TreeNodeField<String> addressNode = drc.getFields().getChildren().get(2)
        
        addressNode.getKey() == "address"
        addressNode.getChildren().size() == 3
        addressNode.getChildren().get(0).getKey() == "id"
        addressNode.getChildren().get(1).getKey() == "address"
        addressNode.getChildren().get(2).getKey() == "country"
        
        TreeNodeField<String> countryNode = addressNode.getChildren().get(2)
        countryNode.getKey() == "country"
        countryNode.getChildren().size() == 1
        countryNode.getChildren().get(0).getKey() == "name"
    }
    
    def "A request with 'fields' query string with invalid format should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id", "address(id,address"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * fieldHelperMessage.fieldRequestMalFormed('fields', 'address(id,address')
    }
    
    def "A request with 'fields' query string with invalid field should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id1"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * crudMessage.fieldRequestDoesNotExistsOnObject('id1', 'org.demoiselle.jee.crud.entity.UserModelForTest')
    }
    
    def "A request with 'fields' query string with invalid field with subfield should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id", "address1(id)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * crudMessage.fieldRequestDoesNotExistsOnObject('address1', 'org.demoiselle.jee.crud.entity.UserModelForTest')
    }
    
    def "A request with 'fields' query string with invalid subfield should throw IllegalArgumentException"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id", "address(idInvalid)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * crudMessage.fieldRequestDoesNotExistsOnObject('idInvalid', 'org.demoiselle.jee.crud.entity.AddressModelForTest')
    }
    
    def "A request with 'fields' query string with a field and subfield and @Search.fields filled should be validated"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["id", "address(address)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        thrown(IllegalArgumentException)
        1 * crudMessage.fieldRequestDoesNotExistsOnSearchField('address(address)')
    }
    
    def "A request with 'fields' query string with a field and subfield and @Search.fields without subfield should be accept"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["name", "address(address,street)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)       
    }
    
    def "A request with 'fields' query string and @Search.fields with '*' value should be accept"(){
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndAllFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["name", "address(address,street)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)
    }
    
}
