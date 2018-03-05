/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.entity.AddressModelForTest
import org.demoiselle.jee.crud.entity.CountryModelForTest
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.field.FieldHelper
import org.demoiselle.jee.crud.field.FieldHelperMessage
import org.demoiselle.jee.crud.filter.FilterHelper
import org.demoiselle.jee.crud.pagination.PaginationHelper
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage
import org.demoiselle.jee.crud.pagination.ResultSet
import org.demoiselle.jee.crud.sort.SortHelper
import org.demoiselle.jee.crud.sort.SortHelperMessage
import spock.lang.Specification

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.UriInfo

/**
 * Test of {@link CrudFilter} class.
 * 
 * @author SERPRO
 */
class CrudFilterSpec extends Specification{
    
    ContainerRequestContext requestContext = Mock()
    ContainerResponseContext responseContext = Mock()
    ResourceInfo resourceInfo = Mock()
    MultivaluedMap mvmRequest = new MultivaluedHashMap()
    MultivaluedMap mvmResponse = new MultivaluedHashMap()
    PaginationHelperMessage paginationMessage = Mock()
    FieldHelperMessage fieldHelperMessage = Mock()
    SortHelperMessage sortHelperMessage = Mock()
    CrudMessage crudMessage = Mock()
    
    DemoiselleRequestContext drc = new DemoiselleRequestContextImpl()
    Result result = new ResultSet()
    DemoiselleCrudConfig crudConfig = Mock()
    
    UriInfo uriInfo = Mock()
    
    SortHelper sortHelper = new SortHelper(resourceInfo, uriInfo, crudConfig, drc, sortHelperMessage, crudMessage)
    PaginationHelper paginationHelper = new PaginationHelper(resourceInfo, uriInfo,  crudConfig, drc, paginationMessage)
    FilterHelper filterHelper = new FilterHelper(resourceInfo, crudConfig, uriInfo, drc, crudMessage)
    FieldHelper fieldHelper = new FieldHelper(resourceInfo, uriInfo, crudConfig, drc, fieldHelperMessage, crudMessage)
    CrudFilter crudFilter = new CrudFilter(resourceInfo, uriInfo, crudConfig, drc, paginationHelper, sortHelper, filterHelper, fieldHelper)
    
    def "A request with 'range' parameter should fill 'Result' object " () {
        
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.isPaginationEnabled() >> true
        mvmRequest.putSingle("range", "10-20")
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        drc.getPaginationContext().getOffset() == 10
        drc.getPaginationContext().getLimit() == 20
        notThrown(RuntimeException)
        
    }
    
    def "A response with 'range', 'sort', 'desc', 'fields' should fill 'Response' object"(){
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        mvmRequest.addAll("sort", ["id", "name"])
        mvmRequest.putSingle("desc", "name")
        mvmRequest.addAll("fields", ["id", "name", "mail", "address(street)"])
        mvmRequest.putSingle("range", "10-20")
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        10.times {
            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
            users << new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com", address: address)
        }
        
        Result result = new ResultSet()
        
        result.getContent().addAll(users)        
        responseContext.getEntity() >> result

        configureRequestForCrud()
        
        def listUsersExpects = [
            ['id':1, 'name':'John0', 'mail':'john0@test.com', 'address':['street':'my street 0']], 
            ['id':1, 'name':'John1', 'mail':'john1@test.com', 'address':['street':'my street 1']], 
            ['id':1, 'name':'John2', 'mail':'john2@test.com', 'address':['street':'my street 2']], 
            ['id':1, 'name':'John3', 'mail':'john3@test.com', 'address':['street':'my street 3']], 
            ['id':1, 'name':'John4', 'mail':'john4@test.com', 'address':['street':'my street 4']], 
            ['id':1, 'name':'John5', 'mail':'john5@test.com', 'address':['street':'my street 5']], 
            ['id':1, 'name':'John6', 'mail':'john6@test.com', 'address':['street':'my street 6']], 
            ['id':1, 'name':'John7', 'mail':'john7@test.com', 'address':['street':'my street 7']], 
            ['id':1, 'name':'John8', 'mail':'john8@test.com', 'address':['street':'my street 8']], 
            ['id':1, 'name':'John9', 'mail':'john9@test.com', 'address':['street':'my street 9']]
        ]
        
        when:
        crudFilter.filter(requestContext)
        crudFilter.filter(requestContext, responseContext)
        
        then:        
        notThrown(RuntimeException)
        mvmResponse.containsKey("Content-Range")
        mvmResponse.containsKey("Accept-Range")
        mvmResponse.containsKey("Link")
        mvmResponse.containsKey("Access-Control-Expose-Headers")
        
        1 * responseContext.setStatus(206)
        1 * responseContext.setEntity(listUsersExpects)
        
    }
    
    def "A request that return all elements should set Response.status with 200 status code"(){
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        drc.count = 10
        
        mvmRequest.addAll("fields", ["id", "name", "mail"])
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        10.times {
            users <<  new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com")
        }
        
        Result result = new ResultSet()
        
        result.getContent().addAll(users)
        responseContext.getEntity() >> result

        configureRequestForCrud()
        
        when:
        crudFilter.filter(requestContext)
        crudFilter.filter(requestContext, responseContext)
        
        then:
        notThrown(RuntimeException)
        mvmResponse.containsKey("Content-Range")
        mvmResponse.containsKey("Accept-Range")
        !mvmResponse.containsKey("Link")
        mvmResponse.containsKey("Access-Control-Expose-Headers")
        
        1 * responseContext.setStatus(200)
    }
    
    def "A request with invalid fields should throw a RuntimeException"() {
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse
        
        configureRequestForCrud()
        
        when:
        crudFilter.filter(requestContext)        
        
        then:
        thrown(RuntimeException)
    }
    
    def "A request without 'fields' defined should use 'fields' from @Search.fields"(){
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        drc.count = 10
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        5.times {
            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
            users << new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com", address: address)
        }
        
        Result result = new ResultSet()
        
        result.getContent().addAll(users)
        responseContext.getEntity() >> result

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        def userExpected = [
            ['name': 'John0', 'address': users[0].address],
            ['name': 'John1', 'address': users[1].address],
            ['name': 'John2', 'address': users[2].address],
            ['name': 'John3', 'address': users[3].address],
            ['name': 'John4', 'address': users[4].address]
        ]
        
        when:
        crudFilter.filter(requestContext)
        crudFilter.filter(requestContext, responseContext)
        
        then:
        notThrown(RuntimeException)
        
        1 * responseContext.setEntity( {
            it == userExpected
        })
        
    }
    
    def "A request that doesn't match with a AbstractREST should does nothing"(){
        
        given:
        resourceInfo.getResourceClass() >> UserRestWithoutAbstractRESTForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> Object.class
        resourceInfo.getResourceMethod() >> UserRestWithoutAbstractRESTForTest.class.getDeclaredMethod("find")
        
        responseContext.getHeaders() >> mvmResponse
        
        when:
        crudFilter.filter(requestContext)
        
        then:
        !mvmResponse.containsKey("Content-Range")
        !mvmResponse.containsKey("Accept-Range")
        !mvmResponse.containsKey("Link")
        !mvmResponse.containsKey("Access-Control-Expose-Headers")
    }

     def "A request that doesn't is for a CRUD and throws a Exception should treat the accept-range header"() {
        given:
        resourceInfo.getResourceClass() >> UserRestWithoutAbstractRESTForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> Object.class
        resourceInfo.getResourceMethod() >> UserRestWithoutAbstractRESTForTest.class.getDeclaredMethod("findWithException")
        
        responseContext.getStatus() >> Status.BAD_REQUEST.statusCode
        drc.entityClass = null
        
        when:
        crudFilter.filter(requestContext, responseContext)
        
        then:
        notThrown(RuntimeException)
        responseContext.status == Status.BAD_REQUEST.statusCode
        1 * responseContext.getEntity()
        !mvmResponse.containsKey("Accept-Range")
    }
    
    def "A request without 'fields' defined should use 'fields' from @Search.fields should respect fields and subfiels"(){
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        drc.count = 10
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        5.times {
            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
            users << new UserModelForTest(id: it, name: "John${it}", mail: "john${it}@test.com", address: address)
        }
        
        Result result = new ResultSet()
        
        result.getContent().addAll(users)
        responseContext.getEntity() >> result

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        def userExpected = [
            ['id': 0, 'name': 'John0', 'address': ['street': users[0].address.street] ],
            ['id': 1, 'name': 'John1', 'address': ['street': users[1].address.street] ],
            ['id': 2, 'name': 'John2', 'address': ['street': users[2].address.street] ],
            ['id': 3, 'name': 'John3', 'address': ['street': users[3].address.street] ],
            ['id': 4, 'name': 'John4', 'address': ['street': users[4].address.street] ]
        ]
        
        when:
        crudFilter.filter(requestContext)
        crudFilter.filter(requestContext, responseContext)
        
        then:
        notThrown(RuntimeException)
        
        1 * responseContext.setEntity( {
            it == userExpected
        })
        
    }
    
    def "A request without 'fields' defined and @Search.fields using '*' should return all fields"() {
        given:
        
        crudConfig.getDefaultPagination() >> 20
        crudConfig.getIsGlobalEnabled() >> true
        
        drc.count = 10
                
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        10.times {
            CountryModelForTest country = new CountryModelForTest(id: it, name: "country ${it}")
            AddressModelForTest address = new AddressModelForTest(id: it, street: "my street ${it}", address: "address ${it}", country: country)
            users << new UserModelForTest(id: it, name: "John${it}", age: it, mail: "john${it}@test.com", address: address)
        }
        
        Result result = new ResultSet()
        
        result.getContent().addAll(users)
        responseContext.getEntity() >> result

        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndAllFields")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
        
        when:
        crudFilter.filter(requestContext)
        crudFilter.filter(requestContext, responseContext)
        
        then:
        notThrown(RuntimeException)
        
        1 * responseContext.setEntity( {
            it == users
        })
    }
    
    private configureRequestForCrud(){
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")
        
        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
    }
}
