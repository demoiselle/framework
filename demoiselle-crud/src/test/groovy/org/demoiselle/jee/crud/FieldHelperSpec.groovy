/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.entity.UserModelForTest

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
    DemoiselleCrudConfig crudConfig = new DemoiselleCrudConfig(false, true, false, false, 20)
    MultivaluedMap mvmRequest = new MultivaluedHashMap<>()
    
    FieldHelper fieldHelper = new FieldHelper(resourceInfo, uriInfo, crudConfig, drc, fieldHelperMessage, crudMessage)
    
    def "A request with 'fields' query string should populate 'DemoiselleRequestContext.fieldsContext.fields'"(){
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields")
        drc.setAbstractRestRequest(true)
        drc.setEntityClass(UserModelForTest.class)
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        mvmRequest.addAll("fields", "id,name")
        
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        drc.getFieldsContext().getFlatFields() != null
        drc.getFieldsContext().getFlatFields().size() == 2

        drc.getFieldsContext().getFlatFields().get(0) == "id"
        drc.getFieldsContext().getFlatFields().get(1) == "name"
    }
    
    def "A request with 'fields' query string and method annotated with @DemoiselleResult should be validated with @DemoiselleResult.filterFields property"(){
        
        given:
        resourceInfo.getResourceClass() >> UserModelForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)
        def fields = UserRestForTest.class.getDeclaredMethod("findWithSearchAndFields").getAnnotation(DemoiselleResult.class).filterFields() as List
        def invalidFields = fields
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        invalidFields << "newInvalidField"
        
        mvmRequest.addAll("fields", invalidFields.join(","))
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        1 * crudMessage.fieldRequestDoesNotExistsOnObject('newInvalidField', 'org.demoiselle.jee.crud.entity.UserModelForTest')
        thrown(IllegalArgumentException)
        
    }
    
    def "A request with 'fields' query string and method annotated with @DemoiselleResult with subfields should be respect @DemoiselleResult.filterFields property"(){
        
        given:
        resourceInfo.getResourceClass() >> UserModelForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

        def invalidFields = ["id", "name", "address"]
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", invalidFields.join(","))
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        
        notThrown(IllegalArgumentException)
        drc.getFieldsContext().getFlatFields() != null
        drc.getFieldsContext().getFlatFields().size() == 3
        
        drc.getFieldsContext().getFlatFields().get(0) == "id"
        drc.getFieldsContext().getFlatFields().get(1) == "name"
        drc.getFieldsContext().getFlatFields().get(2) == "address"

    }
    
    def "A request with 'fields' query string and method that haven't @DemoiselleResult annotation should be executed" () {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
        drc.getFieldsContext().getFlatFields() != null
        drc.getFieldsContext().getFlatFields().get(0) == "id"
        drc.getFieldsContext().getFlatFields().get(1) == "name"
        drc.getFieldsContext().getFlatFields().size() == 2
    }
    
    def "A request with 'fields' query string can be fields with subfields like field(subField1,subField2,subField2(subSubField1))"(){
        given:
        crudConfig.isFilterFields() >> true
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
        drc.getFieldsContext().getFlatFields() != null
        drc.getFieldsContext().getFlatFields().size() == 5
        
        drc.getFieldsContext().getFlatFields().get(0) == "id"
        drc.getFieldsContext().getFlatFields().get(1) == "name"
        drc.getFieldsContext().getFlatFields().get(2) == "address.id"
        drc.getFieldsContext().getFlatFields().get(3) == "address.address"
        drc.getFieldsContext().getFlatFields().get(4) == "address.country.name"
    }
    
    def "A request with 'fields' query string with invalid format should throw IllegalArgumentException"() {
        given:
        crudConfig.isFilterFields() >> true
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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

//    TODO
//    def "A request with 'fields' query string with a field and subfield and @DemoiselleResult.filterFields filled should be validated"() {
//        given:
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields")
//        drc.setResultType(UserModelForTest.class)
//        drc.setAbstractRestRequest(true)
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        mvmRequest.addAll("fields", ["id", "address(address)"])
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        when:
//        fieldHelper.execute(resourceInfo, uriInfo)
//
//        then:
//        thrown(IllegalArgumentException)
//        1 * crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField('address(address)')
//    }

    def "A request with 'fields' query string with a field and subfield and @DemoiselleResult.filterFields without subfield should be accept"() {
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        mvmRequest.addAll("fields", ["name", "address(address,street)"])
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        when:
        fieldHelper.execute(resourceInfo, uriInfo)
        
        then:
        notThrown(IllegalArgumentException)       
    }
    
    def "A request with 'fields' query string and @DemoiselleResult.filterFields with '*' value should be accept"(){
        given:
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndAllFields")
        drc.setEntityClass(UserModelForTest.class)
        drc.setAbstractRestRequest(true)

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
