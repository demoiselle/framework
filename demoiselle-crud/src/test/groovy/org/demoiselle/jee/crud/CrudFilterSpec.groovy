/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.demoiselle.jee.core.api.crud.Result
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig
import org.demoiselle.jee.crud.entity.AddressModelForTest
import org.demoiselle.jee.crud.entity.CountryModelForTest
import org.demoiselle.jee.crud.entity.UserModelForTest
import org.demoiselle.jee.crud.field.FieldHelper
import org.demoiselle.jee.crud.field.FieldHelperMessage
import org.demoiselle.jee.crud.fields.FieldsContext
import org.demoiselle.jee.crud.filter.FilterHelper
import org.demoiselle.jee.crud.pagination.PaginationContext
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
import java.lang.reflect.ParameterizedType
import java.util.function.Function

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
    
    def "A request with 'range' parameter should fill 'DemoiselleCrudHelper.paginationContext' object " () {
        
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

    def "'DemoiselleRequestContext.isAbstractRestRequest()' should be true if the resource class inherits AbstractREST" () {
        when: "the resource class extends AbstractREST"
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        crudFilter.filter(requestContext)

        then: "isAbstractRequest() should be true"
        drc.isAbstractRestRequest() == true
    }

    def "'DemoiselleRequestContext.isAbstractRestRequest()' should be false if the resource class does not inherit AbstractREST" () {
        when: "the resource class extends AbstractREST"
        resourceInfo.getResourceClass() >> UserRestWithoutAbstractRESTForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        crudFilter.filter(requestContext)

        then: "isAbstractRequest() should be true"
        drc.isAbstractRestRequest() == false
    }

    def "'DemoiselleRequestContext.getDemoiselleResultAnnotation()' should return the annotation of 'resourceMethod' if it's present" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithAnnotation")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()

        when:
        crudFilter.filter(requestContext)

        then:
        drc.demoiselleResultAnnotation.pageSize() == 100
    }

    def "'DemoiselleRequestContext.getDemoiselleResultAnnotation()' should return the annotation of resource class if there is none in the method itself" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithoutAnnotation")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        configureRequestForCrud()

        when:
        crudFilter.filter(requestContext)

        then:
        drc.demoiselleResultAnnotation.pageSize() == 50
    }

    def "'DemoiselleRequestContext.getEntityClass()' should return the entityClass argument from the @DemoiselleResult annotation" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findAnotherEntityClass")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        drc.entityClass == Long.class
    }

    def "'DemoiselleRequestContext.getEntityClass()' should return the type argument from AbstractREST if no @DemoiselleResult annotation is present" () {
        resourceInfo.getResourceClass() >> UserRestWithoutAnnotationForTest.class
        resourceInfo.getResourceMethod() >> UserRestWithoutAnnotationForTest.class.getDeclaredMethod("find")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        drc.entityClass == UserModelForTest.class
    }

    def "'DemoiselleRequestContext.getResultClass()' should return the resultClass argument from the @DemoiselleResult annotation" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findAnotherEntityClass")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        drc.resultClass == Integer.class
    }


    def "'DemoiselleRequestContext.getResultTransformer()' should return an identity function if none is provided" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        Object o = new Object()
        drc.resultTransformer.apply(o) == o
    }

    def "'DemoiselleRequestContext.getResultTransformer()' should return the result transformer from the annotation if present" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findResultTransformer")


        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        Long ln = 10L
        drc.resultTransformer.apply(ln) == 11L
    }

    def "The request filter should call all helper classes" () {
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")

        crudFilter.filterHelper = Mock(FilterHelper.class)
        crudFilter.sortHelper = Mock(SortHelper.class)
        crudFilter.paginationHelper = Mock(PaginationHelper.class)
        crudFilter.fieldHelper = Mock(FieldHelper.class)

        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
        uriInfo.getQueryParameters() >> mvmRequest

        when:
        crudFilter.filter(requestContext)

        then:
        1 * crudFilter.filterHelper.execute(resourceInfo, uriInfo)
        1 * crudFilter.sortHelper.execute(resourceInfo, uriInfo)
        1 * crudFilter.paginationHelper.execute(resourceInfo, uriInfo)
        1 * crudFilter.fieldHelper.execute(resourceInfo, uriInfo)
    }

    def "A request that return all elements should set Response.status with 200 status code"(){
        given:

        mvmRequest.addAll("fields", ["id", "name", "mail"])
        uriInfo.getQueryParameters() >> mvmRequest
        
        responseContext.getHeaders() >> mvmResponse

        def users = []
        
        10.times {
            users <<  new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com")
        }

        PaginationContext paginationContext = new PaginationContext()
        paginationContext.setPaginationEnabled(true)
        paginationContext.setLimit(10)
        paginationContext.setOffset(0)
        FieldsContext fieldsContext = FieldsContext.disabledFields()
        ResultSet result = ResultSet.forList(users, UserModelForTest.class, paginationContext, fieldsContext)
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
    
//    def "A request with invalid fields should throw a RuntimeException"() {
//        crudConfig.getDefaultPagination() >> 20
//        crudConfig.getIsGlobalEnabled() >> true
//
//        mvmRequest.addAll("fields", ["id", "name", "invalidField"])
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        responseContext.getHeaders() >> mvmResponse
//
//        configureRequestForCrud()
//
//        when:
//        crudFilter.filter(requestContext)
//
//        then:
//        thrown(RuntimeException)
//    }
//
//    def "A request without 'fields' defined should use 'fields' from @Search.fields"(){
//        given:
//
//        crudConfig.getDefaultPagination() >> 20
//        crudConfig.getIsGlobalEnabled() >> true
//
//        drc.count = 10
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        responseContext.getHeaders() >> mvmResponse
//
//        def users = []
//
//        5.times {
//            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
//            users << new UserModelForTest(id: 1, name: "John${it}", mail: "john${it}@test.com", address: address)
//        }
//
//        Result result = new ResultSet()
//
//        result.getContent().addAll(users)
//        responseContext.getEntity() >> result
//
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearch")
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        def userExpected = [
//            ['name': 'John0', 'address': users[0].address],
//            ['name': 'John1', 'address': users[1].address],
//            ['name': 'John2', 'address': users[2].address],
//            ['name': 'John3', 'address': users[3].address],
//            ['name': 'John4', 'address': users[4].address]
//        ]
//
//        when:
//        crudFilter.filter(requestContext)
//        crudFilter.filter(requestContext, responseContext)
//
//        then:
//        notThrown(RuntimeException)
//
//        1 * responseContext.setEntity( {
//            it == userExpected
//        })
//
//    }
    
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
        2 * responseContext.getEntity()
        !mvmResponse.containsKey("Accept-Range")
    }
    
//    def "A request without 'fields' defined should use 'fields' from @Search.fields should respect fields and subfiels"(){
//        given:
//
//        crudConfig.getDefaultPagination() >> 20
//        crudConfig.getIsGlobalEnabled() >> true
//
//        drc.count = 10
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        responseContext.getHeaders() >> mvmResponse
//
//        def users = []
//
//        5.times {
//            AddressModelForTest address = new AddressModelForTest(street: "my street ${it}")
//            users << new UserModelForTest(id: it, name: "John${it}", mail: "john${it}@test.com", address: address)
//        }
//
//        Result result = new ResultSet()
//
//        result.getContent().addAll(users)
//        responseContext.getEntity() >> result
//
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndFieldsWithSubFields")
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        def userExpected = [
//            ['id': 0, 'name': 'John0', 'address': ['street': users[0].address.street] ],
//            ['id': 1, 'name': 'John1', 'address': ['street': users[1].address.street] ],
//            ['id': 2, 'name': 'John2', 'address': ['street': users[2].address.street] ],
//            ['id': 3, 'name': 'John3', 'address': ['street': users[3].address.street] ],
//            ['id': 4, 'name': 'John4', 'address': ['street': users[4].address.street] ]
//        ]
//
//        when:
//        crudFilter.filter(requestContext)
//        crudFilter.filter(requestContext, responseContext)
//
//        then:
//        notThrown(RuntimeException)
//
//        1 * responseContext.setEntity( {
//            it == userExpected
//        })
//
//    }
//
//    def "A request without 'fields' defined and @Search.fields using '*' should return all fields"() {
//        given:
//
//        crudConfig.getDefaultPagination() >> 20
//        crudConfig.getIsGlobalEnabled() >> true
//
//        drc.count = 10
//
//        uriInfo.getQueryParameters() >> mvmRequest
//
//        responseContext.getHeaders() >> mvmResponse
//
//        def users = []
//
//        10.times {
//            CountryModelForTest country = new CountryModelForTest(id: it, name: "country ${it}")
//            AddressModelForTest address = new AddressModelForTest(id: it, street: "my street ${it}", address: "address ${it}", country: country)
//            users << new UserModelForTest(id: it, name: "John${it}", age: it, mail: "john${it}@test.com", address: address)
//        }
//
//        Result result = new ResultSet()
//
//        result.getContent().addAll(users)
//        responseContext.getEntity() >> result
//
//        resourceInfo.getResourceClass() >> UserRestForTest.class
//        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
//        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("findWithSearchAndAllFields")
//
//        URI uri = new URI("http://localhost:9090/api/users")
//        uriInfo.getRequestUri() >> uri
//
//        when:
//        crudFilter.filter(requestContext)
//        crudFilter.filter(requestContext, responseContext)
//
//        then:
//        notThrown(RuntimeException)
//
//        1 * responseContext.setEntity( {
//            it == users
//        })
//    }
//
    private configureRequestForCrud(){
        resourceInfo.getResourceClass() >> UserRestForTest.class
        resourceInfo.getResourceClass().getSuperclass() >> AbstractREST.class
        resourceInfo.getResourceMethod() >> UserRestForTest.class.getDeclaredMethod("find")

        URI uri = new URI("http://localhost:9090/api/users")
        uriInfo.getRequestUri() >> uri
    }
}
